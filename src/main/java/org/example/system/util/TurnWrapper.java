package org.example.system.util;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class TurnWrapper {
    final static int TURN_DISTANCE = 10000;

    private int age = 0;

    private List<TurnObject> objects = new ArrayList<>();

    public static void main(String[] args) {
        TestObject object = new TestObject();
        TurnWrapper turnWrapper = new TurnWrapper();

        turnWrapper.addObject(object);
    }

    public void addObject(TurnObject object){
        getObjects().add(object);
    }

    public TurnObject nextObjectTurn(){
        if(objects.isEmpty()) return null;

        final Optional<TurnObject> readyObject = objects.stream()
            .filter(TurnObject::readyForTurn)
            .max(Comparator.comparingInt(TurnObject::getPassage));

        if(readyObject.isPresent()){
            return readyObject.get();
        }

        age++;
        final Optional<TurnObject> possibleObject = objects.stream()
            .filter(TurnObject::stepOnce)
            .max(Comparator.comparingInt(TurnObject::getPassage));

        return possibleObject.orElseGet(this::nextObjectTurn);


    }


    @Getter
    @Setter
    public static class TestObject extends TurnObject{
        int speed = 105;


    }

    @Getter
    @Setter
    public static abstract class TurnObject {
        int speed = 100;
        int passage = 0;

        public boolean readyForTurn(){
            return passage/TURN_DISTANCE > 0;
        }
        public boolean stepOnce(){
            passage+=speed;
            return readyForTurn();
        }
    }
}
