package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoldenExporterTicketItem extends ExporterTicketItem {

    public GoldenExporterTicketItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).tab(ItemBlockReg.tab));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("jackseconomy.golden_exporter_manifest_description").withStyle(ChatFormatting.GOLD));
    }
}
