package com.loks.predict.util.impl;

import com.google.common.base.Function;
import com.loks.predict.util.ResourceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;


public class AsyncFileResourceUtility implements ResourceUtility {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFileResourceUtility.class);

    private final Integer BUFFER_SIZE = 262144;

    @Override
    public <T> Mono<T> getData(final String filePath, final Function<ByteArrayOutputStream, T> function) {
        return readAllBytes(Paths.get(filePath)).flatMap(new Function<ByteArrayOutputStream, Mono<T>>(){
            @Nullable
            @Override
            public Mono<T> apply(@Nullable ByteArrayOutputStream byteArrayOutputStream) {
                return Mono.justOrEmpty(function.apply(byteArrayOutputStream));
            }
        });
    }

    @Override
    public <T> Mono<T> getData(final String filePath, final Map<String, String> params, Function<ByteArrayOutputStream, T> function) {
        return getData(filePath, function);
    }

    protected Mono<ByteArrayOutputStream> readAllBytes(final Path filePath){
        try {
            final File file = filePath.toFile();
            final Integer fileSize = (int)file.length();
            logger.info(String.format("File : '%s' with Size : %d", file.getName(), fileSize));
            final ByteBuffer buffer = ByteBuffer.allocate(fileSize);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
            return Mono.fromFuture(readAllBytes(asyncFile, buffer, 0, out)
                            .whenComplete((pos, ex) -> closeAfc(asyncFile , buffer)).thenApply(position -> out));
        }catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void closeAfc(final AsynchronousFileChannel asyncFile, final ByteBuffer byteBuffer) {
        try {
            asyncFile.close();
            byteBuffer.clear();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    protected CompletableFuture<Integer> readAllBytes(
            final AsynchronousFileChannel asyncFile,
            final ByteBuffer buffer,
            final int position,
            final ByteArrayOutputStream out){
        return  readToByteArrayStream(asyncFile, buffer, position, out)
                .thenCompose(index ->
                        index < 0
                                ? completedFuture(position)
                                : readAllBytes(asyncFile, (ByteBuffer)buffer.clear(), position + index, out));

    }

    protected CompletableFuture<Integer> readToByteArrayStream(
            final AsynchronousFileChannel asyncFile,
            final ByteBuffer buffer,
            final int position,
            final ByteArrayOutputStream out){
        CompletableFuture<Integer> promise = new CompletableFuture<>();
        asyncFile.read(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(final Integer result, final ByteBuffer attachment) {
                if(result > 0) {
                    attachment.flip();
                    byte[] data = new byte[attachment.limit()]; // limit = result
                    attachment.get(data);
                    write(out, data);
                }
                promise.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                promise.completeExceptionally(exc);
            }
        });
        return promise;
    }

    protected void write(final ByteArrayOutputStream out, final byte[] data) {
        try {
            out.write(data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
