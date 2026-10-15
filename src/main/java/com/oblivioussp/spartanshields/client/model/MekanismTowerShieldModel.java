package com.oblivioussp.spartanshields.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class MekanismTowerShieldModel extends ShieldBaseModel 
{
	protected static final String PART_PLATE_EXTRA = "plate_extra";
	protected static final String PART_UPPER = "plate_upper";
	protected static final String PART_LOWER = "plate_lower";
	
	public ModelPart plateExtra;
	public ModelPart upper;
	public ModelPart lower;
	
	private final ImmutableList<ModelPart> extraParts;

    public MekanismTowerShieldModel(ModelPart rootModel)
    {
    	super(RenderType::entityTranslucent, rootModel);
    	
    	plateExtra = rootModel.getChild(PART_PLATE_EXTRA);
    	upper = rootModel.getChild(PART_UPPER);
    	lower = rootModel.getChild(PART_LOWER);
    	
    	extraParts = ImmutableList.of(plateExtra, upper, lower);
    }
    
    public static LayerDefinition createLayer()
    {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition platePart = part.addOrReplaceChild(PART_PLATE, CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -7.0f, -2.0f, 12.0f, 17.0f, 1.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_HANDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f), PartPose.ZERO);
		platePart.addOrReplaceChild("plate_lower_main", CubeListBuilder.create().texOffs(0, 17).addBox(-6.0f, 0.0f, 0.0f, 12.0f, 5.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 10.0f, -2.0f, 0.5236f, 0.0f, 0.0f));
		part.addOrReplaceChild(PART_PLATE_EXTRA, CubeListBuilder.create()/*.texOffs(0, 0).addBox(-6.0f, -7.0f, -2.0f, 12.0f, 17.0f, 1.0f)*/
				.texOffs(59, 16).addBox(7.5f, -6.5f, -1.0f, 1.0f, 15.0f, 1.0f)
				.texOffs(50, 0).addBox(5.5f, -7.0f, -2.5f, 1.0f, 16.0f, 3.0f)
				.texOffs(59, 0).addBox(-8.5f, -6.5f, -1.0f, 1.0f, 15.0f, 1.0f)
				.texOffs(42, 0).addBox(-6.5f, -7.0f, -2.5f, 1.0f, 16.0f, 3.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_UPPER, CubeListBuilder.create().texOffs(0, 29).addBox(-9.0f, -3.0f, -0.5f, 4.0f, 3.0f, 2.0f)
				.texOffs(38, 29).addBox(5.0f, -3.0f, -0.5f, 4.0f, 3.0f, 2.0f)
				.texOffs(0, 34).addBox(-9.0f, -2.0f, 1.5f, 4.0f, 2.0f, 2.0f)
				.texOffs(38, 34).addBox(5.0f, -2.0f, 1.5f, 4.0f, 2.0f, 2.0f)
				.texOffs(2, 24).addBox(-8.0f, -6.0f, -0.5f, 2.0f, 3.0f, 2.0f)
				.texOffs(40, 24).addBox(6.0f, -6.0f, -0.5f, 2.0f, 3.0f, 2.0f)
				.texOffs(12, 24).addBox(-6.0f, -6.0f, 0.0f, 12.0f, 6.0f, 1.0f), PartPose.offsetAndRotation(0.0f, -7.0f, -2.0f, -0.5236f, 0.0f, 0.0f));
		part.addOrReplaceChild(PART_LOWER, CubeListBuilder.create().texOffs(0, 42).addBox(-9.0f, -1.0f, -0.5f, 4.0f, 3.0f, 2.0f)
				.texOffs(0, 38).addBox(-9.0f, -1.0f, 1.5f, 4.0f, 2.0f, 2.0f)
				.texOffs(2, 47).addBox(-8.0f, 2.0f, -0.5f, 3.0f, 4.0f, 2.0f)
				.texOffs(38, 47).addBox(5.0f, 2.0f, -0.5f, 3.0f, 4.0f, 2.0f)
				.texOffs(38, 38).addBox(5.0f, -1.0f, 1.5f, 4.0f, 2.0f, 2.0f)
				.texOffs(38, 42).addBox(5.0f, -1.0f, -0.5f, 4.0f, 3.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 10.0f, -2.0f, 0.5236f, 0.0f, 0.0f));
		
		
		return LayerDefinition.create(mesh, 64, 64);
    }
    
    @Override
    public void renderExtraParts(PoseStack mStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn,
			int color) 
    {
    	super.renderExtraParts(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	extraParts.forEach((model) -> {
    		model.render(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	});
    }
    
    @Override
    public void renderLayers(PoseStack mStack, MultiBufferSource bufferIn, RenderType renderTypeIn, int packedLightIn, int packedOverlayIn,
    		int color)
    {
    	VertexConsumer consumer = bufferIn.getBuffer(renderTypeIn);
    	root.render(mStack, consumer, packedLightIn, packedOverlayIn, color);
    }
}
