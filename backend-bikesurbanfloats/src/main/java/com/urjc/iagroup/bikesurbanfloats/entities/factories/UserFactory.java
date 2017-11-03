package com.urjc.iagroup.bikesurbanfloats.entities.factories;

import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserTest;

public class UserFactory {

    public User createUser(UserType type) {
        switch (type) {
            case USER_TEST:
                return new UserTest();
        }
        throw new IllegalArgumentException("The type" + type + "doesn't exists");
    }
}
