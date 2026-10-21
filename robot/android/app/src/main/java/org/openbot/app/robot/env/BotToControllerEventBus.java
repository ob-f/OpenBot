package org.openbot.app.robot.env;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.json.JSONObject;

public final class BotToControllerEventBus {

  private static final PublishSubject<JSONObject> subject = PublishSubject.create();

  public static @NonNull Disposable subscribe(
      @NonNull Consumer<? super JSONObject> onNext, @NonNull Consumer<? super Throwable> onError) {
    return subject
        .toFlowable(BackpressureStrategy.BUFFER)
        .observeOn(Schedulers.computation())
        .subscribe(onNext, onError);
  }

  public static void emitEvent(JSONObject event) {
    subject.onNext(event);
  }

  private BotToControllerEventBus() {}
}
