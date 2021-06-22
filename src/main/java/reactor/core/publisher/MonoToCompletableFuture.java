package reactor.core.publisher;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

class MonoToCompletableFuture<T> extends CompletableFuture<T> implements CoreSubscriber<T> {
    static Logger logger = LoggerFactory.getLogger(MonoToCompletableFuture.class);
    final AtomicReference<Subscription> ref = new AtomicReference<>();
    final boolean cancelSourceOnNext;

     MonoToCompletableFuture(boolean sourceCanEmitMoreThanOnce) {
        this.cancelSourceOnNext = sourceCanEmitMoreThanOnce;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = super.cancel(mayInterruptIfRunning);
        if (cancelled) {
            Subscription s = ref.getAndSet(null);
            if (s != null) {
                s.cancel();
            }
        }
        return cancelled;
    }

    @Override
    public void onSubscribe(Subscription s) {
        logger.info("onSubscribe this:{} ref:{}", this,ref.get()==null?"null":ref.get().toString());
        if (Operators.validate(ref.getAndSet(s), s)) {
            s.request(Long.MAX_VALUE);
        }
        else {
            s.cancel();
        }
    }

    @Override
    public void onNext(T t) {
        logger.info("onNext this:{} ref:{}", this,ref.get()==null?"null":ref.get().toString());
        Subscription s = ref.getAndSet(null);
        if (s != null) {
            complete(t);
            if (cancelSourceOnNext) {
                s.cancel();
            }
        }
        else {
            Operators.onNextDropped(t, currentContext());

        }
    }

    @Override
    public void onError(Throwable t) {
        logger.info("onError this:{} ref:{}", this,ref.get()==null?"null":ref.get().toString());

        if (ref.getAndSet(null) != null) {
            completeExceptionally(t);
        }
    }

    @Override
    public void onComplete() {
        logger.info("onComplete this:{} ref:{}", this,ref.get()==null?"null":ref.get().toString());
        if (ref.getAndSet(null) != null) {
            complete(null);
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        logger.info("get start this:{} ref:{}", this,ref.get()==null?"null":ref.get().toString());
        T t = super.get();
        logger.info("get end this:{} ref:{} result:{}",this.toString(),ref.get()==null?"null":ref.get(),t==null?"null":t.toString());
        return t;
    }

    @Override
    public Context currentContext() {
        return Context.empty();
    }
}