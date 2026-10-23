package com.oblivioussp.spartanshields.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class KiteShieldModel extends ShieldBaseModel 
{
//	public ModelRenderer plateLeft;
//  public ModelRenderer plateRight;
	
	protected static final String PART_PLATE_LEFT = "plate_left";
	protected static final String PART_PLATE_RIGHT = "plate_right";
	
	public ModelPart plateLeft;
	public ModelPart plateRight;

    public KiteShieldModel(ModelPart rootModel)
    {
    	super(rootModel);
/*		plateLeft = new ModelRenderer(this, 52, 0);
		plateLeft.setRotationPoint(-6.0F, 0.0F, 0.0F);
		setRotationAngle(plateLeft, 0.0F, 0.0F, -0.1963F);
		plateLeft.addBox(-1.85f, -10.788f, -1.99f, 4, 20, 1, false);

		plateRight = new ModelRenderer(this, 42, 0);
		plateRight.setRotationPoint(6.0F, 0.0F, 0.0F);
		setRotationAngle(plateRight, 0.0F, 0.0F, 0.1963F);
		plateRight.addBox(-2.15f, -10.788f, -1.99f, 4, 20, 1, false);*/
    	
    	plateLeft = rootModel.getChild(PART_PLATE_LEFT);
    	plateRight = rootModel.getChild(PART_PLATE_RIGHT);
    }
    
    public static LayerDefinition createLayer()
    {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild(PART_PLATE, CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -11.0f, -2.0f, 12.0f, 22.0f, 1.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_HANDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_PLATE_LEFT, CubeListBuilder.create().texOffs(52, 0).addBox(-1.85f, -10.788f, -1.99f, 4.0f, 20.0f, 1.0f), PartPose.offsetAndRotation(-6.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1963f));
		part.addOrReplaceChild(PART_PLATE_RIGHT, CubeListBuilder.create().texOffs(42, 0).addBox(-2.15f, -10.788f, -1.99f, 4.0f, 20.0f, 1.0f), PartPose.offsetAndRotation(6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1963f));
		
		return LayerDefinition.create(mesh, 64, 64);
    }
    
    @Override
    public void renderExtraParts(PoseStack mStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn,
			int color) 
    {
    	super.renderExtraParts(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	plateLeft.render(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	plateRight.render(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    }
    
/*    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn,
    		int color)
    {
    	super.extractRenderState(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	plateLeft.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn);
    	plateRight.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn);
    }
	
	protected void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) 
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}*/
}
