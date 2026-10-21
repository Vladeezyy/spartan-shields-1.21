package com.oblivioussp.spartanshields.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class TowerShieldModel extends ShieldBaseModel
{
//    public ModelRenderer plateLeft;
//    public ModelRenderer plateRight;

	protected static final String PART_PLATE_LEFT = "plate_left";
	protected static final String PART_PLATE_RIGHT = "plate_right";
	
    public ModelPart plateLeft;
    public ModelPart plateRight;

    public TowerShieldModel(ModelPart rootModel)
    {
    	/*super();
        this.plateLeft = new ModelRenderer(this, 42, 0);
        this.plateLeft.setRotationPoint(-6.0f, 0.0f, -2.0f);
        this.plateLeft.rotateAngleY = 45.0f;
        this.plateLeft.addBox(-2.0f, -11.0f, 0.0f, 2, 22, 1, 0.0f);
        this.plateRight = new ModelRenderer(this, 48, 0);
        this.plateRight.setRotationPoint(6.0f, 0.0f, -2.0f);
        this.plateRight.rotateAngleY = -45.0f;
        this.plateRight.addBox(0.0f, -11.0f, 0.0f, 2, 22, 1, 0.0f);*/
    	
    	super(rootModel);
    	
    	plateLeft = rootModel.getChild(PART_PLATE_LEFT);
    	plateRight = rootModel.getChild(PART_PLATE_RIGHT);
    }
    
    public static LayerDefinition createLayer()
    {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild(PART_PLATE, CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -11.0f, -2.0f, 12.0f, 22.0f, 1.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_HANDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_PLATE_LEFT, CubeListBuilder.create().texOffs(42, 0).addBox(-2.0f, -11.0f, 0.0f, 2.0f, 22.0f, 1.0f), PartPose.offsetAndRotation(-6.0f, 0.0f, -2.0f, 0.0f, 0.785398f, 0.0f));
		part.addOrReplaceChild(PART_PLATE_RIGHT, CubeListBuilder.create().texOffs(48, 0).addBox(0.0f, -11.0f, 0.0f, 2.0f, 22.0f, 1.0f), PartPose.offsetAndRotation(6.0f, 0.0f, -2.0f, 0.0f, -0.785398f, 0.0f));
		
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
    
    /*@Override
    public void renderExtraParts(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLightIn,
    		int packedOverlayIn)
    {
    	this.plateLeft.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn);
    	this.plateRight.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn);
    }
    
    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn,
    		int color)
    {
    	super.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	this.plateLeft.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn);
    	this.plateRight.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn);
    }*/
}
