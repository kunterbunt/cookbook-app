package com.kunterbunt.cookbook.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implements a recipe data object.
 * A recipe can have _id==NOT_SAVED, in that case call recipe.setId() to attempt to retrieve the _id with which you can find the recipe in the database.
 * If it then still has _id==NOT_SAVED it is not saved in the database yet. This can happen as recipes can be built step by step and are saved when completed.
 */
public class Recipe implements Parcelable {

    /** Indicates this recipe has no attached images. */
    public static final String NO_IMAGE = "none";
    /** Indicates this recipe has not been saved to the database. */
    public static final long NOT_SAVED = -1;

    private String name, imagePath;
    private List<String> preparationSteps;
    private List<Ingredient> ingredients;
    private List<Category> categories;
    private int preparationTime;
    private int difficulty;
    private int listPosition;
    private float rating;
    private long dateCreated;
    private long _id;

    public Recipe() {
        name = "no name";
        imagePath = NO_IMAGE;
        preparationSteps = new ArrayList<String>(3);
        ingredients = new ArrayList<Ingredient>(5);
        categories = new ArrayList<>(1);
        preparationTime = -1;
        difficulty = -1;
        rating = -1;
        listPosition = 0;
        dateCreated = new Date().getTime();
        _id = NOT_SAVED;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeList(categories);
        parcel.writeList(preparationSteps);
        parcel.writeInt(preparationTime);
        parcel.writeInt(difficulty);
        parcel.writeLong(dateCreated);
        parcel.writeList(ingredients);
    }

    public Recipe(Parcel parcel) {
        name = parcel.readString();
        categories = new ArrayList<Category>();
        parcel.readList(categories, null);
        preparationSteps = new ArrayList<String>();
        parcel.readList(preparationSteps, null);
        preparationTime = parcel.readInt();
        difficulty = parcel.readInt();
        dateCreated = parcel.readLong();
        parcel.readList(ingredients, null);
    }

    public static final Parcelable.Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel parcel) {
            return new Recipe(parcel);
        }

        @Override
        public Recipe[] newArray(int i) {
            return new Recipe[i];
        }
    };

    public long getId() {
        return _id;
    }

    /**
     * Searches the database for a recipe with the same name, ingredients and preparation steps and sets the own _id if it finds a matching one.
     * @return _id, can be NOT_SAVED.
     */
    public long setId() {
        List<Recipe> recipes = DatabaseHelper.getInstance().getAllRecipes();
        for (Recipe recipe : recipes)
            if (recipe.getName().equals(name))
                if (recipe.getPreparationSteps().equals(preparationSteps))
                    if (recipe.getIngredients().equals(ingredients)) {
                        _id = recipe.getId();
                        return _id;
                    }
        _id = NOT_SAVED;
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getPreparationSteps() {
        return preparationSteps;
    }

    public void setPreparationSteps(List<String> preparationSteps) {
        this.preparationSteps = preparationSteps;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

//    public static final String XmlTagName = "name", XmlTagIngredients = "ingredients", XmlTagIngredient = "ingredient",
//            XmlTagPreparation = "preparation", XmlTagPreparationStep = "step", XmlTagRoot = "recipe",
//            XmlTagTime = "time", XmlTagRating = "rating", XmlTagDateCreated = "date_created", XmlTagImagePath = "image_path",
//            XmlTagDifficulty = "difficulty", XmlTagCategory = "category";

    //    public Recipe(String xmlFile, Context context) {
//        this();
//        String data = null;
//        try {
//            File dir = Tools.getRecipeFolder(context);
//            File file = new File(dir, xmlFile);
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            StringBuilder text = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null)
//                text.append(line + "\n");
//            data = text.toString();
//            reader.close();
//        } catch (Exception e) {
//            Log.e(LOG_TAG, e.getMessage());
//        }
//        XmlPullParserFactory factory = null;
//        XmlPullParser parser = null;
//        try {
//            factory = XmlPullParserFactory.newInstance();
//            factory.setNamespaceAware(true);
//            parser = factory.newPullParser();
//            parser.setInput(new StringReader(data));
//            int eventType = parser.getEventType();
//            String currentTag = null;
//            while (eventType != XmlPullParser.END_DOCUMENT) {
//                if (eventType == XmlPullParser.START_TAG) {
//                    currentTag = parser.getName();
//                } else if (eventType == XmlPullParser.END_TAG) {
//                    eventType = parser.next();
//                    continue;
//                } else if (eventType == XmlPullParser.TEXT) {
//                    if (currentTag.equals(XmlTagName))
//                        name = parser.getText();
//                    else if (currentTag.equals(XmlTagCategory))
//                        category = parser.getText();
//                    else if (currentTag.equals(XmlTagIngredient))
//                        ingredients.add(new Ingredient(parser.getText()));
//                    else if (currentTag.equals(XmlTagPreparationStep))
//                        preparationSteps.add(parser.getText());
//                    else if (currentTag.equals(XmlTagDateCreated))
//                        dateCreated = Long.parseLong(parser.getText());
//                    else if (currentTag.equals(XmlTagImagePath))
//                        imagePath = parser.getText();
//                    else if (currentTag.equals(XmlTagDifficulty))
//                        difficulty = Integer.parseInt(parser.getText());
//                    else if (currentTag.equals(XmlTagRating))
//                        rating = Float.parseFloat(parser.getText());
//                    else if (currentTag.equals(XmlTagTime))
//                        preparationTime = Integer.parseInt(parser.getText());
//                    else
//                        Log.e(LOG_TAG, "Unknown XML tag: " + currentTag + "\nText: " + parser.getText());
//                }
//                eventType = parser.next();
//            }
//        } catch (XmlPullParserException e) {
//            Log.e(LOG_TAG, e.getMessage());
//        } catch (IOException e) {
//            Log.e(LOG_TAG, e.getMessage());
//        }
//
//
//    }

//    public String toXML() throws IOException {
//        XmlSerializer xmlSerializer = Xml.newSerializer();
//        StringWriter writer = new StringWriter();
//        xmlSerializer.setOutput(writer);
//        // Start document.
//        xmlSerializer.startDocument("UTF-8", true);
//        xmlSerializer.startTag("", XmlTagRoot);
//        // Set name.
//        xmlSerializer.startTag("", XmlTagName);
//        xmlSerializer.text(name);
//        xmlSerializer.endTag("", XmlTagName);
//        // Set ingredients.
//        xmlSerializer.startTag("", XmlTagIngredients);
//        xmlSerializer.attribute("", "number", "" + ingredients.size());
//        xmlSerializer.attribute("", "type", "array");
//        for (Ingredient ingredient : ingredients) {
//            xmlSerializer.startTag("", XmlTagIngredient);
//            xmlSerializer.text(ingredient.toString());
//            xmlSerializer.endTag("", XmlTagIngredient);
//        }
//        xmlSerializer.endTag("", XmlTagIngredients);
//        // Set preparation steps.
//        xmlSerializer.startTag("", XmlTagPreparation);
//        xmlSerializer.attribute("", "number", "" + preparationSteps.size());
//        xmlSerializer.attribute("", "type", "array");
//        for (String step : preparationSteps) {
//            xmlSerializer.startTag("", XmlTagPreparationStep);
//            xmlSerializer.text(step);
//            xmlSerializer.endTag("", XmlTagPreparationStep);
//        }
//        xmlSerializer.endTag("", XmlTagPreparation);
//        // Set rating.
//        xmlSerializer.startTag("", XmlTagRating);
//        xmlSerializer.text("" + rating);
//        xmlSerializer.endTag("", XmlTagRating);
//        // Set preparation time.
//        xmlSerializer.startTag("", XmlTagTime);
//        xmlSerializer.text("" + preparationTime);
//        xmlSerializer.endTag("", XmlTagTime);
//        // Set difficulty.
//        xmlSerializer.startTag("", XmlTagDifficulty);
//        xmlSerializer.text("" + difficulty);
//        xmlSerializer.endTag("", XmlTagDifficulty);
//        // Set image path.
//        xmlSerializer.startTag("", XmlTagImagePath);
//        xmlSerializer.text(imagePath);
//        xmlSerializer.endTag("", XmlTagImagePath);
//        // Set time created.
//        xmlSerializer.startTag("", XmlTagDateCreated);
//        xmlSerializer.text("" + dateCreated);
//        xmlSerializer.endTag("", XmlTagDateCreated);
//        // Set category.
//        xmlSerializer.startTag("", XmlTagCategory);
//        xmlSerializer.text(category);
//        xmlSerializer.endTag("", XmlTagCategory);
//        // Finish.
//        xmlSerializer.endTag("", XmlTagRoot);
//        xmlSerializer.flush();
//        return writer.toString();
//    }
}
