package presentation.viewmodel.openloopfueling;

import data.contract.Me7LogFileContract;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import domain.math.map.Map3d;
import org.apache.commons.math3.util.Pair;
import data.parser.afrLog.AfrLogParser;
import data.parser.bin.BinParser;
import data.parser.me7log.OpenLoopLogParser;
import data.parser.xdf.TableDefinition;
import data.preferences.mlhfm.MlhfmPreferences;
import data.writer.BinWriter;
import presentation.viewmodel.closedloopfueling.ClosedLoopFuelingViewModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OpenLoopFuelingViewModel {
    private final BehaviorSubject<OpenLoopMlfhmModel> behaviorSubject = BehaviorSubject.create();

    public OpenLoopFuelingViewModel () {

        BinWriter.getInstance().register(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull TableDefinition tableDefinition) {
                if (tableDefinition.getTableName().contains("MLHFM")) {

                    OpenLoopMlfhmModel model = behaviorSubject.getValue();
                    OpenLoopMlfhmModel.Builder builder;
                    if (model == null) {
                        builder = new OpenLoopMlfhmModel.Builder();
                    } else {
                        builder = new OpenLoopMlfhmModel.Builder(model);
                    }

                    builder.logsTabEnabled(true);
                    builder.hasMe7Logs(false);
                    builder.hasAfrLogs(false);
                    builder.selectedTabIndex(0);

                    behaviorSubject.onNext(builder.build());
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });

        BinParser.getInstance().registerMapListObserver(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull List<Pair<TableDefinition, Map3d>> pairs) {
                Pair<TableDefinition, Map3d> tableDefinition = MlhfmPreferences.getInstance().getSelectedMap();
                OpenLoopMlfhmModel model = behaviorSubject.getValue();
                OpenLoopMlfhmModel.Builder builder;
                if (model == null) {
                    builder = new OpenLoopMlfhmModel.Builder();
                } else {
                    builder = new OpenLoopMlfhmModel.Builder(model);
                }

                builder.logsTabEnabled(tableDefinition != null);
                builder.selectedTabIndex(0);

                behaviorSubject.onNext(builder.build());
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });

        MlhfmPreferences.getInstance().registerOnMapChanged(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull Optional<Pair<TableDefinition, Map3d>> tableDefinitionMap3dPair) {
                OpenLoopMlfhmModel model = behaviorSubject.getValue();
                if(tableDefinitionMap3dPair.isPresent()) {
                    OpenLoopMlfhmModel.Builder builder;
                    if (model == null) {
                        builder = new OpenLoopMlfhmModel.Builder();
                    } else {
                        builder = new OpenLoopMlfhmModel.Builder(model);
                    }

                    builder.logsTabEnabled(true);
                    builder.hasMe7Logs(false);
                    builder.hasAfrLogs(false);
                    builder.selectedTabIndex(0);

                    behaviorSubject.onNext(builder.build());
                } else {
                    OpenLoopMlfhmModel.Builder builder;
                    if (model == null) {
                        builder = new OpenLoopMlfhmModel.Builder();
                    } else {
                        builder = new OpenLoopMlfhmModel.Builder(model);
                    }

                    builder.logsTabEnabled(false);
                    builder.hasMe7Logs(false);
                    builder.hasAfrLogs(false);
                    builder.selectedTabIndex(0);

                    behaviorSubject.onNext(builder.build());
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        OpenLoopLogParser.getInstance().register(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull Map<Me7LogFileContract.Header, List<Double>> logs) {
                OpenLoopMlfhmModel model = behaviorSubject.getValue();
                OpenLoopMlfhmModel.Builder builder;
                if (model == null) {
                    builder = new OpenLoopMlfhmModel.Builder();
                } else {
                    builder = new OpenLoopMlfhmModel.Builder(model);
                }

                builder.hasMe7Logs(!logs.isEmpty());
                builder.selectedTabIndex(1);

                behaviorSubject.onNext(builder.build());
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });

        AfrLogParser.getInstance().register(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull Map<String, List<Double>> logs) {
                OpenLoopMlfhmModel model = behaviorSubject.getValue();
                OpenLoopMlfhmModel.Builder builder;
                if (model == null) {
                    builder = new OpenLoopMlfhmModel.Builder();
                } else {
                    builder = new OpenLoopMlfhmModel.Builder(model);
                }

                builder.hasAfrLogs(!logs.isEmpty());
                builder.selectedTabIndex(1);

                behaviorSubject.onNext(builder.build());
            }

            @Override
            public void onError(@NonNull Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void registerMLHFMOnChange(Observer<OpenLoopMlfhmModel> observer) {
        behaviorSubject.subscribe(observer);
    }

    public static class OpenLoopMlfhmModel {
        private final int selectedTabIndex;
        private final boolean logsTabEnabled;
        private final boolean hasAfrLogs;
        private final boolean hasMe7Logs;

        private OpenLoopMlfhmModel(OpenLoopMlfhmModel.Builder builder) {
            this.selectedTabIndex = builder.selectedTabIndex;
            this.logsTabEnabled = builder.logsTabEnabled;
            this.hasAfrLogs = builder.hasAfrLogs;
            this.hasMe7Logs = builder.hasMe7Logs;
        }

        public int getSelectedTabIndex() { return selectedTabIndex; }

        public boolean isLogsTabEnabled() {
            return logsTabEnabled;
        }

        public boolean isCorrectionsTabEnabled() {
            return this.hasAfrLogs && this.hasMe7Logs;
        }

        public static class Builder {
            private int selectedTabIndex;
            private boolean logsTabEnabled;
            private boolean hasAfrLogs;
            private boolean hasMe7Logs;

            public Builder() {}

            public Builder(OpenLoopMlfhmModel model) {
                this.selectedTabIndex = model.selectedTabIndex;
                this.logsTabEnabled = model.logsTabEnabled;
                this.hasAfrLogs = model.hasAfrLogs;
                this.hasMe7Logs = model.hasMe7Logs;
            }

            public OpenLoopMlfhmModel.Builder selectedTabIndex(int selectedTabIndex) {
                this.selectedTabIndex = selectedTabIndex;

                return this;
            }

            public OpenLoopMlfhmModel.Builder logsTabEnabled(boolean enabled) {
                this.logsTabEnabled = enabled;

                return this;
            }

            public OpenLoopMlfhmModel.Builder hasAfrLogs(boolean hasLogs) {
                this.hasAfrLogs = hasLogs;

                return this;
            }

            public OpenLoopMlfhmModel.Builder hasMe7Logs(boolean hasLogs) {
                this.hasMe7Logs = hasLogs;

                return this;
            }

            public OpenLoopMlfhmModel build() {
                return new OpenLoopMlfhmModel(this);
            }
        }
    }
}
