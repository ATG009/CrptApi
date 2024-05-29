import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public void createDocument(String document, String signature) throws InterruptedException {
        lock.lock();
        try {
            while (requestCount.get() >= requestLimit) {
                lock.unlock();
                timeUnit.sleep(1);
                lock.lock();
            }
            requestCount.incrementAndGet();

            System.out.println("Симуляция HTTP POST запроса на: https://ismp.crpt.ru/api/v3/lk/documents/create");
            System.out.println("Документ: " + document);
            System.out.println("Подпись: " + signature);

            System.out.println("Документ успешно создан!");
        } finally {
            requestCount.decrementAndGet();
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        for (int i = 0; i < 10; i++) {
            final int requestNumber = i + 1;
            new Thread(() -> {
                try {
                    api.createDocument("Документ " + requestNumber, "Подпись " + requestNumber);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}