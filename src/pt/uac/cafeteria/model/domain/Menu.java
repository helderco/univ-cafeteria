
package pt.uac.cafeteria.model.domain;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Represents the Meal Menu.
 *
 */
public class Menu {

    public static class Id {
        /** Day of the menu */
        private final Calendar day;

        /** Time of the meal */
        private final Meal.Time time;

        public Id(Calendar day, Meal.Time time) {
            this.day = day;
            this.time = time;
        }

        public Calendar getDay() {
            return (Calendar) this.day.clone();
        }

        public Meal.Time getTime() {
            return this.time;
        }
    }


    private final Id id;

    /** Meat meal */
    private String meat;

    /** Fish meal */
    private String fish;

    /** Vegetarian meal */
    private String veggie;

    /** Soup meal */
    private String soup;

    /** Dessert Meal */
    private String dessert;


    /**
     *  Implements Builder Pattern
     */
    public static class Builder {

        private final Calendar day;

        private final Meal.Time time;

        private String meat;
        private String fish;
        private String veggie;
        private String soup;
        private String dessert;

        public Builder(Calendar day, Meal.Time time) {
            this.day = day;
            this.time = time;
        }

        public Builder setMeatCourse(String meat) {
            this.meat = meat;
            return this;
        }

        public Builder setFishCourse(String fish) {
            this.fish = fish;
            return this;
        }

        public Builder setVeggieCourse(String veggie) {
            this.veggie = veggie;
            return this;
        }

        public Builder setSoupAndDessert (String soup, String dessert) {
            this.soup = soup;
            this.dessert = dessert;
            return this;
        }

        public Menu build() {
            if (this.meat == null && this.fish == null && this.veggie == null) {
                throw new IllegalStateException("É necessário introduzir no mínimo um tipo de prato.");
            }
            if (this.soup == null || this.dessert == null) {
                throw new IllegalStateException("É necessário introduzir uma sopa e sobremesa.");
            }
            return new Menu(this);
        }
    }

    /**
     * Meal Menu Constructor
     *
     * @param builder
     */
    private Menu(Builder builder) {
        this.id = new Id(builder.day, builder.time);
        this.meat = builder.meat;
        this.fish = builder.fish;
        this.veggie = builder.veggie;
        this.soup = builder.soup;
        this.dessert = builder.dessert;
    }


    /** Returns meat meal*/
    public Meal getMeatMeal() {
        return new Meal(id.getDay(), id.getTime(), Meal.Type.MEAT, soup, meat, dessert);
    }

    /** Returns fish meal*/
    public Meal getFishMeal() {
        return new Meal(id.getDay(), id.getTime(), Meal.Type.FISH, soup, fish, dessert);
    }

    /** Returns vegetarian meal*/
    public Meal getVeggieMeal() {
        return new Meal(id.getDay(), id.getTime(), Meal.Type.VEGETARIAN, soup, veggie, dessert);
    }

    /** Returns the main courses */
    public List<String[]> getMainCourses() {
        return Arrays.asList(new String[][] {
            {Meal.Type.MEAT.toString(), this.meat},
            {Meal.Type.FISH.toString(), this.fish},
            {Meal.Type.VEGETARIAN.toString(), this.veggie}
        });
    }

    public String getMainCourse(Meal.Type type) {
        switch (type) {
            case MEAT : return this.meat;
            case FISH : return this.fish;
            case VEGETARIAN : return this.veggie;
            default : return null;
        }
    }

    public String getDessert() {
        return dessert;
    }

    public String getFish() {
        return fish;
    }

    public String getMeat() {
        return meat;
    }

    public String getSoup() {
        return soup;
    }

    public String getVegetarian() {
        return veggie;
    }

    public Id getId() {
        return id;
    }

    public Meal.Time getTime() {
        return id.getTime();
    }
}