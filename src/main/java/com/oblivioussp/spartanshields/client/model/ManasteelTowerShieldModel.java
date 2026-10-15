package com.oblivioussp.spartanshields.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ManasteelTowerShieldModel extends ShieldBaseModel 
{
	protected static final String PART_PLATE_EXTRA = "plate_extra";
	
	public ModelPart plateExtra;

    public ManasteelTowerShieldModel(ModelPart rootModel)
    {
    	super(rootModel);
    	
    	plateExtra = rootModel.getChild(PART_PLATE_EXTRA);
    }
    
    public static LayerDefinition createLayer()
    {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild(PART_PLATE, CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -11.0f, -2.0f, 12.0f, 22.0f, 1.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_HANDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f), PartPose.ZERO);
		part.addOrReplaceChild(PART_PLATE_EXTRA, CubeListBuilder.create().texOffs(50, 0).addBox(6.0f, -10.0f, -2.0f, 1.0f, 20.0f, 1.0f)
				.texOffs(46, 2).addBox(7.0f, -8.0f, -2.0f, 1.0f, 15.0f, 1.0f)
				.texOffs(42, 4).addBox(8.0f, -6.0f, -2.0f, 1.0f, 10.0f, 1.0f)
				.texOffs(42, 17).addBox(-7.0f, -10.0f, -2.0f, 1.0f, 20.0f, 1.0f)
				.texOffs(46, 19).addBox(-8.0f, -8.0f, -2.0f, 1.0f, 15.0f, 1.0f)
				.texOffs(50, 21).addBox(-9.0f, -6.0f, -2.0f, 1.0f, 10.0f, 1.0f), PartPose.ZERO);
		
		return LayerDefinition.create(mesh, 64, 64);
    }
    
    @Override
    public void renderExtraParts(PoseStack mStack, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn,
			int color) 
    {
    	super.renderExtraParts(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    	plateExtra.render(mStack, vertexBuilder, packedLightIn, packedOverlayIn, color);
    }
}
