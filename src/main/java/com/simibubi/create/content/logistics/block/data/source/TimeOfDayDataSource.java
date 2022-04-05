package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererScreen.LineBuilder;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimeOfDayDataSource extends SingleLineDataSource {

	public static final MutableComponent EMPTY_TIME = new TextComponent("--:--");
	
	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		if (!(context.level()instanceof ServerLevel sLevel))
			return EMPTY_TIME;
		if (!(context.getSourceTE() instanceof CuckooClockTileEntity ccte))
			return EMPTY_TIME;
		if (ccte.getSpeed() == 0)
			return EMPTY_TIME;

		boolean c12 = context.sourceConfig()
			.getInt("Cycle") == 0;
		boolean isNatural = sLevel.dimensionType()
			.natural();

		int dayTime = (int) (sLevel.getDayTime() % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int minutes = (dayTime % 1000) * 60 / 1000;
		MutableComponent suffix = Lang.translate("generic.daytime." + (hours > 11 ? "pm" : "am"));

		minutes = minutes / 5 * 5;
		if (c12) {
			hours %= 12;
			if (hours == 0)
				hours = 12;
		}

		if (!isNatural) {
			hours = Create.RANDOM.nextInt(70) + 24;
			minutes = Create.RANDOM.nextInt(40) + 60;
		}

		MutableComponent component = new TextComponent(
			(hours < 10 ? " " : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + (c12 ? " " : ""));

		return c12 ? component.append(suffix) : component;
	}

	@Override
	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		return "Instant";
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DataGathererContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "instant", false, false);
	}

	@Override
	protected String getTranslationKey() {
		return "time_of_day";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DataGathererContext context, LineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;

		builder.addSelectionScrollInput(0, 60, (si, l) -> {
			si.forOptions(Lang.translatedOptions("data_source.time", "12_hour", "24_hour"))
				.titled(Lang.translate("data_source.time.format"));
		}, "Cycle");
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return true;
	}

}
