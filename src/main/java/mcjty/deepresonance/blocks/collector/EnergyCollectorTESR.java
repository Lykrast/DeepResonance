package mcjty.deepresonance.blocks.collector;

import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.blocks.generator.GeneratorConfiguration;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.client.RenderHelper.Vector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class EnergyCollectorTESR extends TileEntitySpecialRenderer<EnergyCollectorTileEntity> {

    ResourceLocation halo = new ResourceLocation(DeepResonance.MODID, "textures/effects/halo.png");
    ResourceLocation laserbeams[] = new ResourceLocation[4];
    Random random = new Random();

    public EnergyCollectorTESR() {
        laserbeams[0] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam1.png");
        laserbeams[1] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam2.png");
        laserbeams[2] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam3.png");
        laserbeams[3] = new ResourceLocation(DeepResonance.MODID, "textures/effects/laserbeam4.png");
    }

    @Override
    public void render(EnergyCollectorTileEntity te, double x, double y, double z, float time, int destroyStage, float alpha) {
        if ((!te.getCrystals().isEmpty()) && (te.areLasersActive() || te.getLaserStartup() > 0)) {
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.85F, (float) z + 0.5F);
            this.bindTexture(halo);
            RenderHelper.renderBillboardQuadBright(1.0f);
            GlStateManager.popMatrix();

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP p = mc.player;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * time;
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * time;
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * time;

            Vector start = new Vector(te.getPos().getX() + .5f, te.getPos().getY() + .5f + .3f, te.getPos().getZ() + .5f);
            Vector player = new Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // ----------------------------------------

            this.bindTexture(laserbeams[random.nextInt(4)]);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
//            tessellator.setBrightness(240);

            float startupFactor = te.getLaserStartup() / (float) GeneratorConfiguration.startupTime;

            for (BlockPos relative : te.getCrystals()) {
                BlockPos destination = new BlockPos(relative.getX() + te.getPos().getX(), relative.getY() + te.getPos().getY(), relative.getZ() + te.getPos().getZ());
                Vector end = new Vector(destination.getX() + .5f, destination.getY() + .5f, destination.getZ() + .5f);

                // @todo Increase jitter if crystals are not pure

                if (startupFactor > .8f) {
                    // Do nothing
                } else if (startupFactor > .001f) {
                    Vector middle = new Vector(jitter(startupFactor, start.x, end.x), jitter(startupFactor, start.y, end.y), jitter(startupFactor, start.z, end.z));
                    RenderHelper.drawBeam(start, middle, player, .1f);
                    RenderHelper.drawBeam(middle, end, player, .1f);
                } else {
                    RenderHelper.drawBeam(start, end, player, .1f);
                }
            }

            tessellator.draw();

            GlStateManager.popMatrix();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private float jitter(float startupFactor, float a1, float a2) {
        return (a1 + a2) / 2.0f + (random.nextFloat() * 2.0f - 1.0f) * startupFactor;
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(EnergyCollectorTileEntity.class, new EnergyCollectorTESR());
    }
}
