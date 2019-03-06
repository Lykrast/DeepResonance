package mcjty.deepresonance.items.armor;

import mcjty.deepresonance.DeepResonance;
import mcjty.deepresonance.api.IRadiationArmor;
import mcjty.deepresonance.radiation.RadiationConfiguration;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by Elec332 on 29-9-2015.
 */
public class ItemRadiationSuit extends ItemArmor implements IRadiationArmor{

    private final String textureSuffix;

    public ItemRadiationSuit(ArmorMaterial material, int renderIndex, EntityEquipmentSlot armorType, String name) {
        super(material, renderIndex, armorType);
        setUnlocalizedName(DeepResonance.MODID + "." + name);
        setRegistryName(name);
        this.textureSuffix = name;
        setCreativeTab(DeepResonance.setup.getTab());
    }

    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advancedToolTip) {
        super.addInformation(itemStack, player, list, advancedToolTip);
        list.add("Every piece of the radiation suit");
        list.add("adds a bit of protection for radiation");
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return DeepResonance.MODID+":textures/items/texture"+textureSuffix+".png";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return RadiationSuitModel.getModel(entityLiving, itemStack);
    }

//    @Override
//    public String getUnlocalizedName() {
//        return super.getUnlocalizedName() + armorType;
//    }
//
//    @Override
//    public String getUnlocalizedName(ItemStack stack) {
//        return getUnlocalizedName();
//    }

    @Override
    public boolean getIsRepairable(ItemStack p_82789_1_, ItemStack p_82789_2_) {
        return false;
    }

    public static int countSuitPieces(EntityLivingBase entity){
        int cnt = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                ItemStack stack = entity.getItemStackFromSlot(slot);
                if (!stack.isEmpty() && (stack.getItem() instanceof IRadiationArmor) && ((IRadiationArmor)stack.getItem()).isActive(stack)) {
                    cnt++;
                } else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("AntiRadiationArmor")) {
                    cnt++;
                }
            }
        }

        return cnt;
    }

    public static float getRadiationProtection(EntityLivingBase entity){
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                ItemStack stack = entity.getItemStackFromSlot(slot);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof IRadiationArmor && ((IRadiationArmor) stack.getItem()).isActive(stack)) {
                        return ((IRadiationArmor) stack.getItem()).protection()[countSuitPieces(entity)];
                    } else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("AntiRadiationArmor")) {
                        return RadiationConfiguration.suitProtection[countSuitPieces(entity)];
                    }
                }
            }
        }
        return 0;
    }
}
