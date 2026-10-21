package com.oblivioussp.spartanshields.client.render.item;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.oblivioussp.spartanshields.client.model.DarkSteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.ElementiumTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.EnderiumShieldModel;
import com.oblivioussp.spartanshields.client.model.KiteShieldModel;
import com.oblivioussp.spartanshields.client.model.LumiumShieldModel;
import com.oblivioussp.spartanshields.client.model.ManasteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.MekanismTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.ShieldBaseModel;
import com.oblivioussp.spartanshields.client.model.TerrasteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.TowerShieldModel;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import com.oblivioussp.spartanshields.ModSpartanShields;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class TowerShieldBEWLR extends BlockEntityWithoutLevelRenderer implements ResourceManagerReloadListener
{
	public static final TowerShieldBEWLR INSTANCE = new TowerShieldBEWLR();
	
	private ShieldBaseModel baseShield;
	private ShieldBaseModel kiteShield;
	private ShieldBaseModel towerShield;
	private ShieldBaseModel lumiumShield;
	private ShieldBaseModel enderiumShield;
	private ShieldBaseModel manasteelShield;
	private ShieldBaseModel terrasteelShield;
	private ShieldBaseModel elementiumShield;
	private ShieldBaseModel mekanismShield;
	private ShieldBaseModel darkSteelShield;
	private Map<Item, ShieldBaseModel> modelMap;
	private Map<Item, TowerShieldRenderInfo> renderInfoMap;
	private TowerShieldBEWLR()
	{
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
		
		Builder<Item, TowerShieldRenderInfo> renderInfoMapBuilder = ImmutableMap.builder();
		renderInfoMapBuilder.put(ModItems.WOODEN_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_WOODEN_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.STONE_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_STONE_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.COPPER_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_COPPER_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.IRON_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_IRON_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.GOLDEN_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_GOLDEN_TOWER_SHIELD_);
		renderInfoMapBuilder.put(ModItems.DIAMOND_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_DIAMOND_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.NETHERITE_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_NETHERITE_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.OBSIDIAN_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_OBSIDIAN_TOWER_SHIELD);

		renderInfoMapBuilder.put(ModItems.TIN_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_TIN_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.BRONZE_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_BRONZE_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.STEEL_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_STEEL_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.SILVER_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_SILVER_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ELECTRUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ELECTRUM_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.LEAD_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_LEAD_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.NICKEL_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_NICKEL_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.INVAR_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_INVAR_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.CONSTANTAN_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_CONSTANTAN_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.PLATINUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_PLATINUM_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ALUMINUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ALUMINUM_TOWER_SHIELD);
		
		renderInfoMapBuilder.put(ModItems.SIGNALUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_SIGNALUM_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.LUMIUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_LUMIUM_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ENDERIUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ENDERIUM_TOWER_SHIELD);
		
		renderInfoMapBuilder.put(ModItems.MANASTEEL_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_MANASTEEL_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.TERRASTEEL_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_TERRASTEEL_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ELEMENTIUM_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ELEMENTIUM_TOWER_SHIELD);
		
		renderInfoMapBuilder.put(ModItems.BASIC_MEKANISTS_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_BASIC_MEKANISTS_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ADVANCED_MEKANISTS_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ADVANCED_MEKANISTS_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ELITE_MEKANISTS_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ELITE_MEKANISTS_TOWER_SHIELD);
		renderInfoMapBuilder.put(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_ULTIMATE_MEKANISTS_TOWER_SHIELD);
		
		renderInfoMapBuilder.put(ModItems.DARK_STEEL_RIOT_TOWER_SHIELD.get(), TextureStitcher.RENDER_INFO_DARK_STEEL_TOWER_SHIELD);
		
		renderInfoMap = renderInfoMapBuilder.build();
	}
	
	@Override
	public void onResourceManagerReload(ResourceManager p_172555_) 
	{
		Minecraft mc = Minecraft.getInstance();
		
		// Reload models here
		baseShield = new ShieldBaseModel(mc.getEntityModels().bakeLayer(ModelLayers.BASE_SHIELD));
		kiteShield = new KiteShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.KITE_SHIELD));
		towerShield = new TowerShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.TOWER_SHIELD));
		lumiumShield = new LumiumShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.LUMIUM_SHIELD));
		enderiumShield = new EnderiumShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.ENDERIUM_SHIELD));
		manasteelShield = new ManasteelTowerShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.MANASTEEL_SHIELD));
		terrasteelShield = new TerrasteelTowerShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.TERRASTEEL_SHIELD));
		elementiumShield = new ElementiumTowerShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.ELEMENTIUM_SHIELD));
		mekanismShield = new MekanismTowerShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.MEKANISM_SHIELD));
		darkSteelShield = new DarkSteelTowerShieldModel(mc.getEntityModels().bakeLayer(ModelLayers.DARK_STEEL_SHIELD));
		// the second (light) pass draws only the faces that show in the light textures
		boolean[] lightsOnOff = {true, false};
		// -Dspartanshields.fullGlow=true: the whole model, as before (dev check of the picture)
		if(!Boolean.getBoolean("spartanshields.fullGlow"))
		{
		mekanismShield.setGlow(com.oblivioussp.spartanshields.client.model.GlowLayer.of(mekanismShield.root(), p_172555_, new ResourceLocation[] {
				ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "textures/entity/mekanism/mekanists_tower_shield_lights_on.png"),
				ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "textures/entity/mekanism/mekanists_tower_shield_lights_off.png")}, lightsOnOff));
		darkSteelShield.setGlow(com.oblivioussp.spartanshields.client.model.GlowLayer.of(darkSteelShield.root(), p_172555_, new ResourceLocation[] {
				ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "textures/entity/enderio/dark_steel_tower_shield_lights_on.png"),
				ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "textures/entity/enderio/dark_steel_tower_shield_lights_off.png")}, lightsOnOff));
		}
		
		Builder<Item, ShieldBaseModel> modelMapBuilder = ImmutableMap.builder();
		modelMapBuilder.put(ModItems.WOODEN_TOWER_SHIELD.get(), baseShield);
		modelMapBuilder.put(ModItems.STONE_TOWER_SHIELD.get(), baseShield);
		modelMapBuilder.put(ModItems.COPPER_TOWER_SHIELD.get(), baseShield);
		modelMapBuilder.put(ModItems.IRON_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.GOLDEN_TOWER_SHIELD.get(), towerShield);
		modelMapBuilder.put(ModItems.DIAMOND_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.NETHERITE_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.OBSIDIAN_TOWER_SHIELD.get(), baseShield);
		
		modelMapBuilder.put(ModItems.TIN_TOWER_SHIELD.get(), baseShield);
		modelMapBuilder.put(ModItems.BRONZE_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.STEEL_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.SILVER_TOWER_SHIELD.get(), towerShield);
		modelMapBuilder.put(ModItems.ELECTRUM_TOWER_SHIELD.get(), towerShield);
		modelMapBuilder.put(ModItems.LEAD_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.NICKEL_TOWER_SHIELD.get(), baseShield);
		modelMapBuilder.put(ModItems.INVAR_TOWER_SHIELD.get(), towerShield);
		modelMapBuilder.put(ModItems.CONSTANTAN_TOWER_SHIELD.get(), towerShield);
		modelMapBuilder.put(ModItems.PLATINUM_TOWER_SHIELD.get(), kiteShield);
		modelMapBuilder.put(ModItems.ALUMINUM_TOWER_SHIELD.get(), baseShield);
		
		modelMapBuilder.put(ModItems.SIGNALUM_TOWER_SHIELD.get(), towerShield);
		modelMapBuilder.put(ModItems.LUMIUM_TOWER_SHIELD.get(), lumiumShield);
		modelMapBuilder.put(ModItems.ENDERIUM_TOWER_SHIELD.get(), enderiumShield);
		
		modelMapBuilder.put(ModItems.MANASTEEL_TOWER_SHIELD.get(), manasteelShield);
		modelMapBuilder.put(ModItems.TERRASTEEL_TOWER_SHIELD.get(), terrasteelShield);
		modelMapBuilder.put(ModItems.ELEMENTIUM_TOWER_SHIELD.get(), elementiumShield);
		
		modelMapBuilder.put(ModItems.BASIC_MEKANISTS_TOWER_SHIELD.get(), mekanismShield);
		modelMapBuilder.put(ModItems.ADVANCED_MEKANISTS_TOWER_SHIELD.get(), mekanismShield);
		modelMapBuilder.put(ModItems.ELITE_MEKANISTS_TOWER_SHIELD.get(), mekanismShield);
		modelMapBuilder.put(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD.get(), mekanismShield);
		
		modelMapBuilder.put(ModItems.DARK_STEEL_RIOT_TOWER_SHIELD.get(), darkSteelShield);
		
		modelMap = modelMapBuilder.build();
	}
	
	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack mStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay)
	{
		ShieldBaseModel model = modelMap.get(stack.getItem());
		TowerShieldRenderInfo renderInfo = renderInfoMap.get(stack.getItem());
		if(model != null && renderInfo != null)
		{
			// 1.20.1: a non-empty "BlockEntityTag"; 1.21 keeps the banner in the base colour and pattern components
			BannerPatternLayers bannerPatterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
			DyeColor baseColour = stack.get(DataComponents.BASE_COLOR);
			boolean isBannered = ShieldBaseItem.hasBanner(stack);
			mStack.pushPose();
			mStack.scale(1.0f, -1.0f, -1.0f);
			
			Material material = renderInfo.getMaterial(isBannered);
			VertexConsumer consumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffer, model.renderType(material.atlasLocation()), true, stack.hasFoil()));
			model.renderExtraParts(mStack, consumer, packedLight, packedOverlay, -1);
			if(isBannered)
			{
				BannerRenderer.renderPatterns(mStack, buffer, packedLight, packedOverlay, model.getPlate(), material, false, Objects.requireNonNullElse(baseColour, DyeColor.WHITE), bannerPatterns, stack.hasFoil());
			}
			else
				model.getPlate().render(mStack, consumer, packedLight, packedOverlay, -1);
			
			if(renderInfo.hasLayers())
				model.renderLayers(mStack, buffer, renderInfo.getLayerRenderType(stack), packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(1.0f, renderInfo.getColourRed(), renderInfo.getColourGreen(), renderInfo.getColourBlue()));
			
			mStack.popPose();
		}
	}
}
