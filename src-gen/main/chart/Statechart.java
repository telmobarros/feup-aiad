
package main.chart;

import java.util.Map;
import java.util.HashMap;

import repast.simphony.statecharts.*;
import repast.simphony.statecharts.generator.GeneratedFor;

import main.*;

@GeneratedFor("_UUQs0NlGEee4wt08Goi-AA")
public class Statechart extends DefaultStateChart<main.Launcher> {

	public static Statechart createStateChart(main.Launcher agent, double begin) {
		Statechart result = createStateChart(agent);
		StateChartScheduler.INSTANCE.scheduleBeginTime(begin, result);
		return result;
	}

	public static Statechart createStateChart(main.Launcher agent) {
		StatechartGenerator generator = new StatechartGenerator();
		return generator.build(agent);
	}

	private Statechart(main.Launcher agent) {
		super(agent);
	}

	private static class MyStateChartBuilder extends StateChartBuilder<main.Launcher> {

		public MyStateChartBuilder(main.Launcher agent, AbstractState<main.Launcher> entryState,
				String entryStateUuid) {
			super(agent, entryState, entryStateUuid);
			setPriority(0.0);
		}

		@Override
		public Statechart build() {
			Statechart result = new Statechart(getAgent());
			setStateChartProperties(result);
			return result;
		}
	}

	private static class StatechartGenerator {

		private Map<String, AbstractState<Launcher>> stateMap = new HashMap<String, AbstractState<Launcher>>();

		public Statechart build(Launcher agent) {
			throw new UnsupportedOperationException("Statechart has not been defined.");

		}

		private void createTransitions(MyStateChartBuilder mscb) {

		}

	}
}
