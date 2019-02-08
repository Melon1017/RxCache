package spring;

import com.safframework.rxcache.RxCache;

import com.safframework.rxcache.domain.Record;
import domain.User;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * Created by tony on 2018/10/5.
 */
public class TestWithSpring {

    public static void main(String[] args) {

        ApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);

        RxCache rxCache = (RxCache) ctx.getBean("rxCache");

        User u = new User();
        u.name = "tony";
        u.password = "123456";
        rxCache.save("test",u);

        Observable<Record<User>> observable = rxCache.load2Observable("test", User.class);

        observable.subscribe(new Consumer<Record<User>>() {

            @Override
            public void accept(Record<User> record) throws Exception {

                User user = record.getData();
                System.out.println(user.name);
                System.out.println(user.password);
            }
        });
    }
}
