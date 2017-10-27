package com.urjc.iagroup.bikesurbanfloats.entities.factories;

import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.User.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.UserTest;

public class UserFactory {

    public User createUser(UserType type, GeoPoint position) {
        switch (type) {
            case USER_TEST:
                return new UserTest(position);
        }
        throw new IllegalArgumentException("The type" + type + "doesn't exists");
    }
}
