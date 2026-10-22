package com.oblivioussp.spartanshields.client.model;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class ShieldBaseModel extends Model<Unit>
{
//	public ModelRenderer plate;
//	public ModelRenderer handle;
	
	protected static final String PART_PLATE = "plate";
	protected static final String PART_HANDLE = "handle";
	
	/** The faces of the light layer to draw, set by the tower shield renderer when it is baked (null: all) */
	protected GlowLayer glow;

	public void setGlow(GlowLayer glowIn)
	{
		glow = glowIn;
	}
	protected ModelPart plate;
	protected ModelPart handle;
	
/*	public ShieldBaseModel()
    {
		super(RenderType::getEntitySolid);
		this.textureWidth = 64;
        this.textureHeight = 64;
        plate = new ModelRenderer(this, 0, 0);
		plate.setRotationPoint(0.0F, 0.0F, 0.0F);
		plate.addBox(-6.0f, -11.0f, -2.0f, 12.0f, 22.0f, 1.0f, 0.0f);

		handle = new ModelRenderer(this, 26, 0);
		handle.setRotationPoint(0.0F, 0.0F, 0.0F);
		handle.addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f, 0.0f);
    }*/
	
	public ShieldBaseModel(Function<Identifier, RenderType> renderType, ModelPart rootModel)
	{
		super(rootModel, renderType);
		plate = rootModel.getChild(PART_PLATE);
		handle = rootModel.getChild(PART_HANDLE);
	}
	
	public ShieldBaseModel(ModelPart rootModel)
	{
		this(RenderTypes::entitySolid, rootModel);
	}
	
	public static LayerDefinition createLayer()
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild(PART_PLATE, CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -11.0f, -2.0f, 12.0f, 22.0f, 1.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_HANDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f), PartPose.ZERO);
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	/*public ModelRenderer getMainPlate()
	{
		return plate;
	}
	
	public ModelRenderer getHandle()
	{
		return handle;
	}*/
	
	public ModelPart getPlate()
	{
		return plate;
	}
	
	public void renderExtraParts(PoseStack mStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn,
			int color) 
	{
		this.handle.render(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
	}
	
	/** The light layer (Mekanist, Dark Steel): only the faces that show in the light textures (see GlowLayer), the whole
	 * model if that could not be set up */
	public void renderLayers(PoseStack mStack, VertexConsumer consumer, int packedLightIn, int packedOverlayIn, int color)
	{
		if(glow != null)
			glow.render(root, mStack, consumer, packedLightIn, packedOverlayIn, color);
		else
			root.render(mStack, consumer, packedLightIn, packedOverlayIn, color);
	}
}
