package com.oblivioussp.spartanshields.client.render.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.client.model.DarkSteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.ElementiumTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.EnderiumShieldModel;
import com.oblivioussp.spartanshields.client.model.GlowLayer;
import com.oblivioussp.spartanshields.client.model.KiteShieldModel;
import com.oblivioussp.spartanshields.client.model.LumiumShieldModel;
import com.oblivioussp.spartanshields.client.model.ManasteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.MekanismTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.ShieldBaseModel;
import com.oblivioussp.spartanshields.client.model.TerrasteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.TowerShieldModel;
import com.oblivioussp.spartanshields.init.ModDataComponents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

/**
 * Draws a tower shield (1.21.1: TowerShieldBEWLR; 1.21.4+ has no BEWLR): the item definition
 * {@code {"type": "minecraft:special", "model": {"type": "spartanshields:tower_shield", "shield": <item id>}}} names
 * the shield, which gives the model and the textures. Handle and extra parts, then the plate with its banner, then
 * the light layer of the Mekanist and Dark Steel shields (only its visible faces, see {@link GlowLayer}).
 */
public class TowerShieldSpecialRenderer implements SpecialModelRenderer<TowerShieldSpecialRenderer.Argument>
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "tower_shield");

	/** What the picture depends on, taken from the stack */
	public record Argument(BannerPatternLayers patterns, DyeColor baseColour, boolean powered)
	{
		boolean hasBanner()
		{
			return baseColour != null || !patterns.layers().isEmpty();
		}
	}

	enum Kind
	{
		BASE(ModelLayers.BASE_SHIELD, ShieldBaseModel::new),
		KITE(ModelLayers.KITE_SHIELD, KiteShieldModel::new),
		TOWER(ModelLayers.TOWER_SHIELD, TowerShieldModel::new),
		LUMIUM(ModelLayers.LUMIUM_SHIELD, LumiumShieldModel::new),
		ENDERIUM(ModelLayers.ENDERIUM_SHIELD, EnderiumShieldModel::new),
		MANASTEEL(ModelLayers.MANASTEEL_SHIELD, ManasteelTowerShieldModel::new),
		TERRASTEEL(ModelLayers.TERRASTEEL_SHIELD, TerrasteelTowerShieldModel::new),
		ELEMENTIUM(ModelLayers.ELEMENTIUM_SHIELD, ElementiumTowerShieldModel::new),
		MEKANISM(ModelLayers.MEKANISM_SHIELD, MekanismTowerShieldModel::new, "mekanism/mekanists_tower_shield"),
		DARK_STEEL(ModelLayers.DARK_STEEL_SHIELD, DarkSteelTowerShieldModel::new, "enderio/dark_steel_tower_shield");

		final ModelLayerLocation layer;
		final Function<ModelPart, ShieldBaseModel> factory;
		/** The light textures (textures/entity/<lights>_lights_on.png and _off.png), or null */
		final String lights;

		Kind(ModelLayerLocation layer, Function<ModelPart, ShieldBaseModel> factory)
		{
			this(layer, factory, null);
		}

		Kind(ModelLayerLocation layer, Function<ModelPart, ShieldBaseModel> factory, String lights)
		{
			this.layer = layer;
			this.factory = factory;
			this.lights = lights;
		}
	}

	private record Shield(Kind kind, TowerShieldRenderInfo info) {}

	private static final Map<Identifier, Shield> SHIELDS = new HashMap<>();

	private static void shield(String id, Kind kind, TowerShieldRenderInfo info)
	{
		SHIELDS.put(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, id), new Shield(kind, info));
	}

	static
	{
		shield("wooden_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_WOODEN_TOWER_SHIELD);
		shield("stone_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_STONE_TOWER_SHIELD);
		shield("copper_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_COPPER_TOWER_SHIELD);
		shield("iron_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_IRON_TOWER_SHIELD);
		shield("golden_tower_shield", Kind.TOWER, TextureStitcher.RENDER_INFO_GOLDEN_TOWER_SHIELD_);
		shield("diamond_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_DIAMOND_TOWER_SHIELD);
		shield("netherite_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_NETHERITE_TOWER_SHIELD);
		shield("obsidian_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_OBSIDIAN_TOWER_SHIELD);
		shield("tin_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_TIN_TOWER_SHIELD);
		shield("bronze_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_BRONZE_TOWER_SHIELD);
		shield("steel_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_STEEL_TOWER_SHIELD);
		shield("silver_tower_shield", Kind.TOWER, TextureStitcher.RENDER_INFO_SILVER_TOWER_SHIELD);
		shield("electrum_tower_shield", Kind.TOWER, TextureStitcher.RENDER_INFO_ELECTRUM_TOWER_SHIELD);
		shield("lead_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_LEAD_TOWER_SHIELD);
		shield("nickel_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_NICKEL_TOWER_SHIELD);
		shield("invar_tower_shield", Kind.TOWER, TextureStitcher.RENDER_INFO_INVAR_TOWER_SHIELD);
		shield("constantan_tower_shield", Kind.TOWER, TextureStitcher.RENDER_INFO_CONSTANTAN_TOWER_SHIELD);
		shield("platinum_tower_shield", Kind.KITE, TextureStitcher.RENDER_INFO_PLATINUM_TOWER_SHIELD);
		shield("aluminum_tower_shield", Kind.BASE, TextureStitcher.RENDER_INFO_ALUMINUM_TOWER_SHIELD);
		shield("signalum_tower_shield", Kind.TOWER, TextureStitcher.RENDER_INFO_SIGNALUM_TOWER_SHIELD);
		shield("lumium_tower_shield", Kind.LUMIUM, TextureStitcher.RENDER_INFO_LUMIUM_TOWER_SHIELD);
		shield("enderium_tower_shield", Kind.ENDERIUM, TextureStitcher.RENDER_INFO_ENDERIUM_TOWER_SHIELD);
		shield("manasteel_tower_shield", Kind.MANASTEEL, TextureStitcher.RENDER_INFO_MANASTEEL_TOWER_SHIELD);
		shield("terrasteel_tower_shield", Kind.TERRASTEEL, TextureStitcher.RENDER_INFO_TERRASTEEL_TOWER_SHIELD);
		shield("elementium_tower_shield", Kind.ELEMENTIUM, TextureStitcher.RENDER_INFO_ELEMENTIUM_TOWER_SHIELD);
		shield("basic_mekanists_tower_shield", Kind.MEKANISM, TextureStitcher.RENDER_INFO_BASIC_MEKANISTS_TOWER_SHIELD);
		shield("advanced_mekanists_tower_shield", Kind.MEKANISM, TextureStitcher.RENDER_INFO_ADVANCED_MEKANISTS_TOWER_SHIELD);
		shield("elite_mekanists_tower_shield", Kind.MEKANISM, TextureStitcher.RENDER_INFO_ELITE_MEKANISTS_TOWER_SHIELD);
		shield("ultimate_mekanists_tower_shield", Kind.MEKANISM, TextureStitcher.RENDER_INFO_ULTIMATE_MEKANISTS_TOWER_SHIELD);
		shield("dark_steel_riot_tower_shield", Kind.DARK_STEEL, TextureStitcher.RENDER_INFO_DARK_STEEL_TOWER_SHIELD);
	}

	private final SpriteGetter sprites;
	private final ShieldBaseModel model;
	/** The plate alone, for the banner layers (they are drawn over the whole model they are given) */
	private final Model.Simple plate;
	private final TowerShieldRenderInfo info;

	private TowerShieldSpecialRenderer(SpriteGetter sprites, ShieldBaseModel model, TowerShieldRenderInfo info)
	{
		this.sprites = sprites;
		this.model = model;
		this.plate = new Model.Simple(model.getPlate(), RenderTypes::entitySolid);
		this.info = info;
	}

	@Override
	public Argument extractArgument(ItemStack stack)
	{
		return new Argument(stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY), stack.get(DataComponents.BASE_COLOR),
				stack.getOrDefault(ModDataComponents.ENERGY.get(), 0) != 0);
	}

	@Override
	public void submit(Argument argument, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay,
			boolean foil, int outline)
	{
		boolean banner = argument != null && argument.hasBanner();
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		SpriteId material = info.getMaterial(banner);
		TextureAtlasSprite sprite = sprites.get(material);
		RenderType renderType = model.renderType(material.atlasLocation());
		collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> model.renderExtraParts(at(pose), sprite.wrap(consumer), light, overlay, -1));
		if(foil)
			collector.submitCustomGeometry(poseStack, RenderTypes.entityGlint(), (pose, consumer) -> model.renderExtraParts(at(pose), sprite.wrap(consumer), light, overlay, -1));
		// 26.1: the banner layers come without the plate under them (vanilla's shield draws its model first)
		collector.submitModelPart(model.getPlate(), poseStack, renderType, light, overlay, sprite, -1, null, outline);
		if(banner)
			BannerRenderer.submitPatterns(sprites, poseStack, collector, light, overlay, plate, Unit.INSTANCE, false,
					Objects.requireNonNullElse(argument.baseColour(), DyeColor.WHITE), argument.patterns(), null);
		// 26.2: submitModelPart has no foil flag; the glint goes over the banner layers, as vanilla's shield does
		if(foil)
			collector.order((banner ? argument.patterns().layers().size() : 0) + 1).submitModelPart(model.getPlate(), poseStack,
					RenderTypes.entityGlint(), light, overlay, sprite, -1, null, 0);
		if(info.hasLayers())
		{
			int colour = ARGB.colorFromFloat(1.0F, info.getColourRed(), info.getColourGreen(), info.getColourBlue());
			// order 1: after the shield itself (as vanilla's energy swirl layer), else the plate covers the added light
			collector.order(1).submitCustomGeometry(poseStack, info.getLayerRenderType(argument != null && argument.powered()),
					(pose, consumer) -> model.renderLayers(at(pose), consumer, light, overlay, colour));
		}
		poseStack.popPose();
	}

	private static PoseStack at(PoseStack.Pose pose)
	{
		PoseStack poseStack = new PoseStack();
		poseStack.last().set(pose);
		return poseStack;
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output)
	{
		PoseStack poseStack = new PoseStack();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		model.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked(Identifier shield) implements SpecialModelRenderer.Unbaked<Argument>
	{
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
				Identifier.CODEC.fieldOf("shield").forGetter(Unbaked::shield)).apply(builder, Unbaked::new));

		@Override
		public MapCodec<Unbaked> type()
		{
			return MAP_CODEC;
		}

		@Override
		public TowerShieldSpecialRenderer bake(SpecialModelRenderer.BakingContext context)
		{
			Shield shield = SHIELDS.get(this.shield);
			if(shield == null)
				return null;
			ShieldBaseModel model = shield.kind().factory.apply(context.entityModelSet().bakeLayer(shield.kind().layer));
			// -Dspartanshields.fullGlow=true: the whole model, as before (dev check of the picture)
			if(shield.kind().lights != null && !Boolean.getBoolean("spartanshields.fullGlow"))
			{
				String lights = "textures/entity/" + shield.kind().lights;
				model.setGlow(GlowLayer.of(model.root(), Minecraft.getInstance().getResourceManager(), new Identifier[] {
						Identifier.fromNamespaceAndPath(ModSpartanShields.ID, lights + "_lights_on.png"),
						Identifier.fromNamespaceAndPath(ModSpartanShields.ID, lights + "_lights_off.png")}, new boolean[] {true, false}));
			}
			return new TowerShieldSpecialRenderer(context.sprites(), model, shield.info());
		}
	}
}
