package mcjty.deepresonance.blocks.collector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.blocks.generator.GeneratorConfiguration;
import mcjty.varia.Coordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class EnergyCollectorTESR extends TileEntitySpecialRenderer {
    IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(DeepResonance.MODID, "obj/collector.obj"));
    ResourceLocation blockTexture = new ResourceLocation(DeepResonance.MODID, "textures/blocks/energyCollector.png");
    ResourceLocation laserbeam = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam.png");
    ResourceLocation laserbeams[] = new ResourceLocation[4];
    Random random = new Random();

    public EnergyCollectorTESR() {
        laserbeams[0] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam1.png");
        laserbeams[1] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam2.png");
        laserbeams[2] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam3.png");
        laserbeams[3] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam4.png");
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float time) {
        bindTexture(blockTexture);

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.0F, (float) z + 0.5F);
//        GL11.glScalef(0.09375F, 0.09375F, 0.09375F);

        model.renderAll();
        GL11.glPopMatrix();

        EnergyCollectorTileEntity energyCollectorTileEntity = (EnergyCollectorTileEntity) tileEntity;

        if ((!energyCollectorTileEntity.getCrystals().isEmpty()) && (energyCollectorTileEntity.areLasersActive() || energyCollectorTileEntity.getLaserStartup() > 0)) {
            boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

            this.bindTexture(laserbeams[random.nextInt(4)]);

            Minecraft mc = Minecraft.getMinecraft();
            EntityClientPlayerMP p = mc.thePlayer;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * time;
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * time;
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * time;

            GL11.glPushMatrix();
            GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

            Tessellator tessellator = Tessellator.instance;

            tessellator.startDrawingQuads();
//            tessellator.setColorRGBA(255, 0, 0, 180);
            tessellator.setBrightness(240);

            float startupFactor = (float) energyCollectorTileEntity.getLaserStartup() / (float) GeneratorConfiguration.startupTime;

            Coordinate thisLocation = new Coordinate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
            for (Coordinate relative : energyCollectorTileEntity.getCrystals()) {
                Coordinate destination = new Coordinate(relative.getX() + tileEntity.xCoord, relative.getY() + tileEntity.yCoord, relative.getZ() + tileEntity.zCoord);
                Vector start = new Vector(thisLocation.getX() + .5f, thisLocation.getY() + .5f + .3f, thisLocation.getZ() + .5f);
                Vector end = new Vector(destination.getX() + .5f, destination.getY() + .5f, destination.getZ() + .5f);
                Vector player = new Vector((float) doubleX, (float) doubleY, (float) doubleZ);

                if (startupFactor > .8f) {
                    // Do nothing
                } else if (startupFactor > .001f) {
                    Vector middle = new Vector(jitter(startupFactor, start.x, end.x), jitter(startupFactor, start.y, end.y), jitter(startupFactor, start.z, end.z));
                    drawLine(start, middle, player);
                    drawLine(middle, end, player);
                } else {
                    drawLine(start, end, player);
                }
            }

            tessellator.draw();

            GL11.glPopMatrix();

            if (!blending) {
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }

    private float jitter(float startupFactor, float a1, float a2) {
        return (a1 + a2) / 2.0f + (random.nextFloat() * 2.0f - 1.0f) * startupFactor;
    }

    private void drawLine(Vector S, Vector E, Vector P) {
        Vector PS = Sub(S, P);
        Vector SE = Sub(E, S);

        Vector normal = Cross(PS, SE);
        normal = normal.normalize();

        Vector half = Mul(normal, .1f);
        Vector p1 = Add(S, half);
        Vector p2 = Sub(S, half);
        Vector p3 = Add(E, half);
        Vector p4 = Sub(E, half);

        drawQuad(Tessellator.instance, p1, p3, p4, p2);
    }

    private void drawQuad(Tessellator tessellator, Vector p1, Vector p2, Vector p3, Vector p4) {
        tessellator.addVertexWithUV(p1.getX(), p1.getY(), p1.getZ(), 0, 0);
        tessellator.addVertexWithUV(p2.getX(), p2.getY(), p2.getZ(), 1, 0);
        tessellator.addVertexWithUV(p3.getX(), p3.getY(), p3.getZ(), 1, 1);
        tessellator.addVertexWithUV(p4.getX(), p4.getY(), p4.getZ(), 0, 1);
    }

    private static class Vector {
        private final float x;
        private final float y;
        private final float z;

        private Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public float norm() {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }

        public Vector normalize() {
            float n = norm();
            return new Vector(x / n, y / n, z / n);
        }
    }

    private static Vector Cross(Vector a, Vector b) {
        float x = a.y*b.z - a.z*b.y;
        float y = a.z*b.x - a.x*b.z;
        float z = a.x*b.y - a.y*b.x;
        return new Vector(x, y, z);
    }

    private static Vector Sub(Vector a, Vector b) {
        return new Vector(a.x-b.x, a.y-b.y, a.z-b.z);
    }
    private static Vector Add(Vector a, Vector b) {
        return new Vector(a.x+b.x, a.y+b.y, a.z+b.z);
    }
    private static Vector Mul(Vector a, float f) {
        return new Vector(a.x * f, a.y * f, a.z * f);
    }


}