package com.example.trainingcompanion.ui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.User;
import com.example.trainingcompanion.ui.fragment.Inform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SettingsViewModel extends ViewModel {
    private Inform inform = null;
    private DBManager dbManager = new DBManager();

    private MutableLiveData<User> user = new MutableLiveData<>(new User(0, 0, 0, ""));

    private MutableLiveData<String> selectedSex = new MutableLiveData<>("");

    public MutableLiveData<User> getUser() {
        return user;
    }

    public MutableLiveData<String> getSelectedSex() {
        return selectedSex;
    }

    public void resetAllDatabase() {
        dbManager.resetAllDatabase();
        inform.onSuccess("Data reset");
    }

    public void resetToDefaults() {
        dbManager.resetDefaultData();
        inform.onSuccess("Reset to defaults");
    }

    public void resetUserData() {
        user.setValue(new User(0, 0, 0, "male"));
        dbManager.resetUserData();
        inform.onSuccess("User data reset");
    }

    public void setUserData() {
        User temp = user.getValue();

        if (temp.getAge() > 5 && temp.getAge() < 120 &&
                temp.getHeight() > 50 && temp.getHeight() < 240 &&
                temp.getWeight() > 15 && temp.getWeight() < 400) {
            if (!Objects.equals(selectedSex.getValue(), "")) {
                dbManager.updateUser(temp);
                inform.onSuccess("User data saved");
            } else {
                inform.onFailure("Select sex");
            }
        } else {
            inform.onFailure("Invalid input");
        }

    }

    public void checkUser() {
        if (dbManager.getUserData().getAge() != 0) {
            user.setValue(dbManager.getUserData());
            selectedSex.setValue(dbManager.getUserData().getSex());
        }
    }

    public ArrayList<String> getChoices() {
        return new ArrayList<>(Arrays.asList("male", "female"));
    }

    public void setSelectedSex(String value) {
        selectedSex.setValue(value);
        Objects.requireNonNull(user.getValue()).setSex(value);
    }

    public void setInform(Inform inf) {
        inform = inf;
    }
}
