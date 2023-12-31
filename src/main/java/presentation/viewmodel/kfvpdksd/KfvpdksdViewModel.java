package presentation.viewmodel.kfvpdksd;

import data.contract.Me7LogFileContract;
import data.parser.bin.BinParser;
import data.parser.me7log.KfvpdksdLogParser;
import data.parser.me7log.Me7LogParser;
import data.parser.xdf.TableDefinition;
import data.preferences.MapPreferenceManager;
import data.preferences.kfvpdksd.KfvpdksdPreferences;
import domain.math.RescaleAxis;
import domain.math.map.Map3d;
import domain.model.kfvpdksd.Kfvpdksd;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import org.apache.commons.math3.util.Pair;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class KfvpdksdViewModel {

    private final BehaviorSubject<KfvpdksdModel> subject = BehaviorSubject.create();

    public KfvpdksdViewModel() {
        BinParser.getInstance().registerMapListObserver(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull List<Pair<TableDefinition, Map3d>> pairs) {
                Pair<TableDefinition, Map3d> kfvpdksdTable = KfvpdksdPreferences.getInstance().getSelectedMap();
                if (kfvpdksdTable != null) {
                    subject.onNext(new KfvpdksdModel(kfvpdksdTable, null, null));
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });

        KfvpdksdPreferences.getInstance().registerOnMapChanged(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {}

            @Override
            public void onNext(@NonNull Optional<Pair<TableDefinition, Map3d>> tableDefinitionMap3dPair) {
                Pair<TableDefinition, Map3d> kfvpdksdTable = KfvpdksdPreferences.getInstance().getSelectedMap();
                if (kfvpdksdTable != null) {
                    subject.onNext(new KfvpdksdModel(kfvpdksdTable, null, null));
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        KfvpdksdLogParser.getInstance().registerLogOnChangeObserver(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull Map<Me7LogFileContract.Header, List<Double>> log) {
                Pair<TableDefinition, Map3d> kfvpdksdTable = KfvpdksdPreferences.getInstance().getSelectedMap();
                if (kfvpdksdTable != null) {
                    calculateKfvpdksd(Kfvpdksd.parsePressure(log, kfvpdksdTable.getSecond().yAxis));
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        });

        MapPreferenceManager.registerOnClear(new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                subject.onNext(new KfvpdksdModel(null, null, null));
            }

            @Override
            public void onError(@NonNull Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void register(Observer<KfvpdksdModel> observer) {
        subject.subscribe(observer);
    }

    public void loadLogs(File file, Me7LogParser.ProgressCallback progressCallback) {
        KfvpdksdLogParser.getInstance().loadDirectory(file, progressCallback);
    }

    public void calculateKfvpdksd(Double[] maxPressure) {
        Pair<TableDefinition, Map3d> kfvpdksdTable = KfvpdksdPreferences.getInstance().getSelectedMap();
        if(kfvpdksdTable != null) {
            Double[] rescaledPressureRatio = RescaleAxis.rescaleAxis(kfvpdksdTable.getSecond().xAxis, (1000+getMax(maxPressure))/1000d);
            kfvpdksdTable.getSecond().xAxis = rescaledPressureRatio;
            Kfvpdksd kfvpdksd = Kfvpdksd.generate(maxPressure, KfvpdksdPreferences.getInstance().getSelectedMap().getSecond().yAxis, rescaledPressureRatio);
            SwingUtilities.invokeLater(() -> subject.onNext(new KfvpdksdModel(kfvpdksdTable, kfvpdksd, maxPressure)));
        }
    }


    private double getMax(Double[] values) {
        return Collections.max(Arrays.asList(values.clone()));
    }

    public record KfvpdksdModel(Pair<TableDefinition, Map3d> kfvpdksdTable, Kfvpdksd kfvpdksd, Double[] pressure) {
        public KfvpdksdModel(@Nullable Pair<TableDefinition, Map3d> kfvpdksdTable, @Nullable Kfvpdksd kfvpdksd, @Nullable Double[] pressure) {
            this.kfvpdksdTable = kfvpdksdTable;
            this.kfvpdksd = kfvpdksd;
            this.pressure = pressure;
        }

        @Nullable
        public Pair<TableDefinition, Map3d> getKfvpdksdTable() {
            return kfvpdksdTable;
        }

        @Nullable
        public Kfvpdksd getKfvpdksd() {
            return kfvpdksd;
        }

        @Nullable
        public Double[] getPressure() {
            return pressure;
        }
    }
}
