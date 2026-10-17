package com.oblivioussp.spartanshields.client.model;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * The light layer of a tower shield (Mekanist, Dark Steel) drawn a second time over the whole model, but only with the
 * faces that show something in the light textures: those textures light up 2-9 % of their area, the other faces were
 * drawn fully transparent (translucent "lights off") or black (additive "lights on"), which leaves no trace. The faces
 * are picked once per resource reload; the picture is the same.
 *
 * <p>{@code ModelPart.Cube} keeps its faces private, so they are read once through reflection; if that fails the whole
 * model is drawn as before.
 */
public final class GlowLayer
{
	/** Per cube: the visible faces, packed as normal (3) then 4 vertices of position (3) and uv (2) */
	private final Map<ModelPart.Cube, float[]> faces = new IdentityHashMap<>();
	private int total, kept;

	private GlowLayer() {}

	/**
	 * @param textures the light textures; {@code additive[i]}: black counts as unlit (eyes render type), otherwise
	 * only full transparency does
	 */
	public static GlowLayer of(ModelPart root, ResourceManager manager, ResourceLocation[] textures, boolean[] additive)
	{
		try
		{
			boolean[][] lit = null;
			int width = 0, height = 0;
			for(int t = 0; t < textures.length; t++)
			{
				try(NativeImage image = NativeImage.read(manager.open(textures[t])))
				{
					if(lit == null)
					{
						width = image.getWidth();
						height = image.getHeight();
						lit = new boolean[width][height];
					}
					for(int x = 0; x < Math.min(width, image.getWidth()); x++)
						for(int y = 0; y < Math.min(height, image.getHeight()); y++)
						{
							int abgr = image.getPixelRGBA(x, y);
							boolean visible = (abgr >>> 24) != 0 && (!additive[t] || (abgr & 0xFFFFFF) != 0);
							lit[x][y] |= visible;
						}
				}
			}
			Field polygonsField = ModelPart.Cube.class.getDeclaredField("polygons");
			polygonsField.setAccessible(true);
			GlowLayer layer = new GlowLayer();
			boolean[][] mask = lit;
			int w = width, h = height;
			root.visit(new PoseStack(), (pose, path, index, cube) -> layer.add(cube, polygonsField, mask, w, h));
			Log.info("Shield light layer " + textures[0] + ": " + layer.kept + " of " + layer.total + " faces");
			return layer;
		}
		catch(Exception | LinkageError e)
		{
			Log.error("Could not prepare a shield light layer, it is drawn whole: " + e);
			return null;
		}
	}

	private void add(ModelPart.Cube cube, Field polygonsField, boolean[][] mask, int width, int height)
	{
		try
		{
			Object[] polygons = (Object[])polygonsField.get(cube);
			float[] packed = new float[polygons.length * 23];
			int n = 0;
			for(Object polygon : polygons)
			{
				total++;
				Field verticesField = polygon.getClass().getDeclaredField("vertices");
				Field normalField = polygon.getClass().getDeclaredField("normal");
				verticesField.setAccessible(true);
				normalField.setAccessible(true);
				Object[] vertices = (Object[])verticesField.get(polygon);
				Vector3f normal = (Vector3f)normalField.get(polygon);
				float[] pos = new float[12], uv = new float[8];
				float minU = 1, minV = 1, maxU = 0, maxV = 0;
				for(int i = 0; i < 4; i++)
				{
					Class<?> vertexClass = vertices[i].getClass();
					Field p = vertexClass.getDeclaredField("pos"), u = vertexClass.getDeclaredField("u"), v = vertexClass.getDeclaredField("v");
					p.setAccessible(true);
					u.setAccessible(true);
					v.setAccessible(true);
					Vector3f vp = (Vector3f)p.get(vertices[i]);
					pos[i * 3] = vp.x();
					pos[i * 3 + 1] = vp.y();
					pos[i * 3 + 2] = vp.z();
					uv[i * 2] = u.getFloat(vertices[i]);
					uv[i * 2 + 1] = v.getFloat(vertices[i]);
					minU = Math.min(minU, uv[i * 2]);
					maxU = Math.max(maxU, uv[i * 2]);
					minV = Math.min(minV, uv[i * 2 + 1]);
					maxV = Math.max(maxV, uv[i * 2 + 1]);
				}
				if(!anyLit(mask, width, height, minU, minV, maxU, maxV))
					continue;
				kept++;
				packed[n++] = normal.x();
				packed[n++] = normal.y();
				packed[n++] = normal.z();
				for(int i = 0; i < 4; i++)
				{
					packed[n++] = pos[i * 3];
					packed[n++] = pos[i * 3 + 1];
					packed[n++] = pos[i * 3 + 2];
					packed[n++] = uv[i * 2];
					packed[n++] = uv[i * 2 + 1];
				}
			}
			if(n > 0)
				faces.put(cube, java.util.Arrays.copyOf(packed, n));
		}
		catch(ReflectiveOperationException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private static boolean anyLit(boolean[][] mask, int width, int height, float minU, float minV, float maxU, float maxV)
	{
		int x0 = Math.max(0, (int)Math.floor(minU * width)), x1 = Math.min(width, (int)Math.ceil(maxU * width));
		int y0 = Math.max(0, (int)Math.floor(minV * height)), y1 = Math.min(height, (int)Math.ceil(maxV * height));
		for(int x = x0; x < x1; x++)
			for(int y = y0; y < y1; y++)
				if(mask[x][y])
					return true;
		return false;
	}

	/** Same vertices as {@code ModelPart.render}, only for the visible faces */
	public void render(ModelPart root, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color)
	{
		Vector3f normal = new Vector3f(), position = new Vector3f();
		root.visit(poseStack, (pose, path, index, cube) ->
		{
			float[] data = faces.get(cube);
			if(data == null)
				return;
			Matrix4f matrix = pose.pose();
			for(int i = 0; i < data.length; i += 23)
			{
				pose.transformNormal(data[i], data[i + 1], data[i + 2], normal);
				for(int v = 0; v < 4; v++)
				{
					int o = i + 3 + v * 5;
					matrix.transformPosition(data[o] / 16.0f, data[o + 1] / 16.0f, data[o + 2] / 16.0f, position);
					consumer.addVertex(position.x(), position.y(), position.z(), color, data[o + 3], data[o + 4], packedOverlay, packedLight,
							normal.x(), normal.y(), normal.z());
				}
			}
		});
	}
}
