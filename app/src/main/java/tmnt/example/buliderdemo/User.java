package tmnt.example.buliderdemo;

import tmnt.example.processer.Bulider;

/**
 * Created by tmnt on 2017/11/16.
 */
@Bulider()
public class User {
    String firstName;
    String lastName;
    String nickName;
    int age;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public int getAge() {
        return age;
    }
}
