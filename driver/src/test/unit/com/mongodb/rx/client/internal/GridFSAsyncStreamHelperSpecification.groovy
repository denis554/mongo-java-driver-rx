package com.mongodb.rx.client.internal

import com.mongodb.Block
import com.mongodb.MongoGridFSException
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.gridfs.AsyncInputStream as WrappedAsyncInputStream
import com.mongodb.async.client.gridfs.AsyncOutputStream as WrappedAsyncOutputStream
import com.mongodb.rx.client.ObservableAdapter
import com.mongodb.rx.client.Success
import com.mongodb.rx.client.gridfs.AsyncInputStream
import com.mongodb.rx.client.gridfs.AsyncOutputStream
import com.mongodb.rx.client.gridfs.helpers.AsyncStreamHelper
import rx.Observable
import rx.observers.TestSubscriber
import spock.lang.Specification

import java.nio.ByteBuffer

import static com.mongodb.async.client.Observables.observe
import static com.mongodb.rx.client.internal.ObservableHelper.voidToSuccessCallback

class GridFSAsyncStreamHelperSpecification extends Specification {

    def 'should call the underlying AsyncInputStream methods'() {
        given:
        def wrapped = Mock(WrappedAsyncInputStream)
        def observableAdapter = Mock(ObservableAdapter)
        def byteBuffer = ByteBuffer.allocate(10)
        def stream = GridFSAsyncStreamHelper.toAsyncInputStream(wrapped, observableAdapter)

        when:
        stream.read(byteBuffer)subscribe(new TestSubscriber())

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * wrapped.read(byteBuffer, _)

        when:
        stream.close().subscribe(new TestSubscriber())

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * wrapped.close(_)
    }

    def 'should call the underlying AsyncOutputStream methods'() {
        given:
        def wrapped = Mock(WrappedAsyncOutputStream)
        def observableAdapter = Mock(ObservableAdapter)
        def byteBuffer = ByteBuffer.allocate(10)
        def stream = GridFSAsyncStreamHelper.toAsyncOutputStream(wrapped, observableAdapter)

        when:
        stream.write(byteBuffer)subscribe(new TestSubscriber())

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * wrapped.write(byteBuffer, _)

        when:
        stream.close()subscribe(new TestSubscriber())

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * wrapped.close(_)
    }

    def 'should call the underlying async library AsyncInputStream methods'() {
        given:
        def wrapped = Mock(AsyncInputStream)
        def callback = Stub(SingleResultCallback)
        def byteBuffer = ByteBuffer.allocate(10)
        def stream = GridFSAsyncStreamHelper.toCallbackAsyncInputStream(wrapped)

        when:
        stream.read(byteBuffer, callback)

        then:
        1 * wrapped.read(byteBuffer) >> Observable.just(1)

        when:
        stream.close(callback)

        then:
        1 * wrapped.close() >> Observable.just(Success)
    }

    def 'should call the underlying async library AsyncOutputStream methods'() {
        given:
        def wrapped = Mock(AsyncOutputStream)
        def callback = Stub(SingleResultCallback)
        def byteBuffer = ByteBuffer.allocate(10)
        def stream = GridFSAsyncStreamHelper.toCallbackAsyncOutputStream(wrapped)

        when:
        stream.write(byteBuffer, callback)

        then:
        1 * wrapped.write(byteBuffer) >> Observable.just(1)


        when:
        stream.close(callback)

        then:
        1 * wrapped.close() >> Observable.just(Success)
    }

    def 'should pass the underlying InputStream values back'() {
        given:
        def inputStream = Mock(InputStream)
        def asyncInputStream = AsyncStreamHelper.toAsyncInputStream(inputStream)
        def observableAdapter = Mock(ObservableAdapter)
        def callbackBasedInputStream = GridFSAsyncStreamHelper.toCallbackAsyncInputStream(asyncInputStream)

        when:
        def subscriber = new TestSubscriber<Integer>()
        RxObservables.create(observe(new Block<SingleResultCallback<Integer>>() {
            @Override
            void apply(final SingleResultCallback<Integer> callback) {
                callbackBasedInputStream.read(ByteBuffer.allocate(1024), callback)
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * inputStream.read(_) >> { 42 }

        then:
        subscriber.assertReceivedOnNext([42])
        subscriber.assertTerminalEvent()

        when:
        subscriber = new TestSubscriber<Success>()
        RxObservables.create(observe(new Block<SingleResultCallback<Void>>() {
            @Override
            void apply(final SingleResultCallback<Void> callback) {
                callbackBasedInputStream.close(voidToSuccessCallback(callback))
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * inputStream.close()

        then:
        subscriber.assertReceivedOnNext([Success.SUCCESS])
        subscriber.assertTerminalEvent()
    }

    def 'should pass the underlying OutputStream values back'() {
        given:
        def outputStream = Mock(OutputStream)
        def asyncOutputStream = AsyncStreamHelper.toAsyncOutputStream(outputStream)
        def observableAdapter = Mock(ObservableAdapter)
        def callbackBasedOutputStream = GridFSAsyncStreamHelper.toCallbackAsyncOutputStream(asyncOutputStream)

        when:
        def subscriber = new TestSubscriber<Integer>()
        RxObservables.create(observe(new Block<SingleResultCallback<Integer>>() {
            @Override
            void apply(final SingleResultCallback<Integer> callback) {
                callbackBasedOutputStream.write(ByteBuffer.allocate(1024), callback)
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * outputStream.write(_)

        then:
        subscriber.assertReceivedOnNext([1024])
        subscriber.assertTerminalEvent()

        when:
        subscriber = new TestSubscriber<Success>()
        RxObservables.create(observe(new Block<SingleResultCallback<Void>>() {
            @Override
            void apply(final SingleResultCallback<Void> callback) {
                callbackBasedOutputStream.close(voidToSuccessCallback(callback))
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * outputStream.close()

        then:
        subscriber.assertReceivedOnNext([Success.SUCCESS])
        subscriber.assertTerminalEvent()
    }

    def 'should handle underlying InputStream errors'() {
        given:
        def inputStream = Mock(InputStream)
        def asyncInputStream = AsyncStreamHelper.toAsyncInputStream(inputStream)
        def observableAdapter = Mock(ObservableAdapter)
        def callbackBasedInputStream = GridFSAsyncStreamHelper.toCallbackAsyncInputStream(asyncInputStream)

        when:
        def subscriber = new TestSubscriber<Integer>()
        RxObservables.create(observe(new Block<SingleResultCallback<Integer>>() {
            @Override
            void apply(final SingleResultCallback<Integer> callback) {
                callbackBasedInputStream.read(ByteBuffer.allocate(1024), callback)
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * inputStream.read(_) >> { throw new IOException('Read failed') }

        then:
        subscriber.assertError(MongoGridFSException)

        when:
        subscriber = new TestSubscriber<Success>()
        RxObservables.create(observe(new Block<SingleResultCallback<Void>>() {
            @Override
            void apply(final SingleResultCallback<Void> callback) {
                callbackBasedInputStream.close(voidToSuccessCallback(callback))
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * inputStream.close() >> { throw new IOException('Closed failed') }

        then:
        subscriber.assertError(MongoGridFSException)
    }

    def 'should handle underlying OutputStream errors'() {
        given:
        def outputStream = Mock(OutputStream)
        def asyncOutputStream = AsyncStreamHelper.toAsyncOutputStream(outputStream)
        def observableAdapter = Mock(ObservableAdapter)
        def callbackBasedOutputStream = GridFSAsyncStreamHelper.toCallbackAsyncOutputStream(asyncOutputStream)

        when:
        def subscriber = new TestSubscriber<Integer>()
        RxObservables.create(observe(new Block<SingleResultCallback<Integer>>() {
            @Override
            void apply(final SingleResultCallback<Integer> callback) {
                callbackBasedOutputStream.write(ByteBuffer.allocate(1024), callback)
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * outputStream.write(_) >> { throw new IOException('Read failed') }

        then:
        subscriber.assertError(MongoGridFSException)

        when:
        subscriber = new TestSubscriber<Success>()
        RxObservables.create(observe(new Block<SingleResultCallback<Void>>() {
            @Override
            void apply(final SingleResultCallback<Void> callback) {
                callbackBasedOutputStream.close(voidToSuccessCallback(callback))
            }
        }), observableAdapter).subscribe(subscriber)

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * outputStream.close() >> { throw new IOException('Closed failed') }

        then:
        subscriber.assertError(MongoGridFSException)
    }


}
