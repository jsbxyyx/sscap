package io.sscap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(),
            60000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger();

                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "Socket #" + count.getAndIncrement());
                }
            }, new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        try {
            Options options = Options.parse(args);
            Ln.initLogLevel(options.logLevel);
            Ln.d("options: " + options);

            Ln.i("server socket start. port: " + options.port + " PID: " + android.os.Process.myPid());
            ServerSocket ss = new ServerSocket(options.port);

            Looper.prepareMainLooper();

            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Socket s = ss.accept();
                        executor.submit(() -> {
                            Looper.prepare();
                            boolean alive = true;

                            try {
                                ByteBuffer banner = ByteBuffer.allocate(24);
                                banner.put((byte) 1);
                                banner.put((byte) 24);
                                banner.putInt(android.os.Process.myPid()); //PID
                                banner.putInt(900);
                                banner.putInt(1600);
                                banner.putInt(900);
                                banner.putInt(1600);
                                banner.put((byte) 0); //as per libui ui::Rotation enum
                                banner.put((byte) 2); //quirk
                                banner.flip();
                                s.getOutputStream().write(banner.array());
                            } catch (IOException e) {
                                Ln.e("send banner exc.", e);
                            }

                            do {
                                try {
                                    long start = System.currentTimeMillis();
                                    Process sh = Runtime.getRuntime().exec("/system/bin/screencap -p");
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    try (BufferedInputStream in = new BufferedInputStream(sh.getInputStream())) {
                                        copy(in, out);
                                    }
                                    sh.destroy();
                                    Ln.d("image : " + out.size() + ", cost : " + (System.currentTimeMillis() - start) + "ms");

                                    ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, options.quality, imageOut);
                                    bitmap.recycle();

                                    byte[] bytes = imageOut.toByteArray();
                                    Ln.d("image out : " + bytes.length);

                                    ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length)
                                            .order(ByteOrder.LITTLE_ENDIAN);
                                    buffer.putInt(bytes.length);
                                    buffer.put(bytes);
                                    buffer.flip();

                                    s.getOutputStream().write(buffer.array());
                                } catch (IOException e) {
                                    Ln.e("send image exc. " + e.getMessage());
                                    alive = false;
                                }
                            } while (alive);
                            try {
                                if (s != null) {
                                    s.close();
                                }
                            } catch (IOException e) {
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "server");
            thread.start();

            Looper.loop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

}
