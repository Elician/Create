package com.simibubi.create.content.logistics.block.data.target;

import java.util.List;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.source.DataGathererSource;
import com.simibubi.create.content.logistics.block.data.source.SingleLineDataSource;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlapDisplayDataTarget extends DataGathererTarget {

	@Override
	public void acceptText(int line, List<MutableComponent> text, DataGathererContext context) {}

	public void acceptFlapText(int line, List<List<MutableComponent>> text, DataGathererContext context) {
		FlapDisplayTileEntity controller = getController(context);
		if (controller == null)
			return;
		if (!controller.isSpeedRequirementFulfilled())
			return;

		DataGathererSource source = context.te().activeSource;
		List<FlapDisplayLayout> lines = controller.getLines();
		for (int i = 0; i + line < lines.size(); i++) {

			if (i == 0)
				reserve(i + line, controller, context);
			if (i > 0 && isReserved(i + line, controller, context))
				break;
			
			FlapDisplayLayout layout = lines.get(i + line);
			
			if (i >= text.size()) {
				if (source instanceof SingleLineDataSource)
					break;
				controller.applyTextManually(i + line, null);
				continue;
			}

			source.loadFlapDisplayLayout(context, controller, layout);

			for (int sectionIndex = 0; sectionIndex < layout.getSections()
				.size(); sectionIndex++) {
				List<MutableComponent> textLine = text.get(i);
				if (textLine.size() <= sectionIndex)
					break;
				layout.getSections()
					.get(sectionIndex)
					.setText(textLine.get(sectionIndex));
			}
		}

		controller.sendData();
	}

	@Override
	public boolean isReserved(int line, BlockEntity target, DataGathererContext context) {
		return super.isReserved(line, target, context)
			|| target instanceof FlapDisplayTileEntity fdte && fdte.manualLines.length > line && fdte.manualLines[line];
	}

	@Override
	public DataTargetStats provideStats(DataGathererContext context) {
		FlapDisplayTileEntity controller = getController(context);
		if (controller == null)
			return new DataTargetStats(1, 1, this);
		return new DataTargetStats(controller.ySize * 2, controller.getMaxCharCount(), this);
	}

	private FlapDisplayTileEntity getController(DataGathererContext context) {
		BlockEntity teIn = context.getTargetTE();
		if (!(teIn instanceof FlapDisplayTileEntity te))
			return null;
		return te.getController();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
		AABB baseShape = super.getMultiblockBounds(level, pos);
		BlockEntity te = level.getBlockEntity(pos);

		if (!(te instanceof FlapDisplayTileEntity fdte))
			return baseShape;

		FlapDisplayTileEntity controller = fdte.getController();
		if (controller == null)
			return baseShape;
		
		Vec3i normal = controller.getDirection()
			.getClockWise()
			.getNormal();
		return baseShape.move(controller.getBlockPos()
			.subtract(pos))
			.expandTowards(normal.getX() * (controller.xSize - 1), 1 - controller.ySize,
				normal.getZ() * (controller.xSize - 1));
	}

}
