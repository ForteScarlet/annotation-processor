package love.forte;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * @author ForteScarlet
 */
public class WeakTest {
    public static void main(String[] args) {
        final WeakHashMap<User, School> map = new WeakHashMap<>();

        User user = new User(2);
        map.put(user, new School(user));

        System.out.println(map);
        user = null;
        for (int i = 0; i < 100; i++) {
            System.gc();
            System.out.println(map);
        }

        System.out.println(map);



    }
}

class School {
    final Reference<User> user;

    School(User user) {
        this.user = new WeakReference<>(user);
    }

    @Override
    public String toString() {
        return "School(" +
                "user=" + user +
                ')';
    }
}

class User {
    final int age;

    User(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User(" +
                "age=" + age +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age;
    }

    @Override
    public int hashCode() {
        return Objects.hash(age);
    }
}
