# Contribution Guide

The application codebase follows a single activity architecture. 

It launches with `MainActivity.java` which holds `MainFragment.java` as it's default fragment. 

`MainFragment.java` has a recyclerview which is used to route to different screens and features using Android's Navigation component. The recycler view adapter is loaded with data curated from `FeatureList.java` class.

The project contains two `abstract` classes `ControlsFragment.java` and `CameraFragment.java` which can be extended to inherit the relvevant functionalities.

#### `ControlsFragment.java`

This class contains all relevant code required to operate the robot via a controller. Just extending this class will give your feature the ability to control the robot in one go. It uses two `private` methods,

```
private void processKeyEvent(KeyEvent keyCode)
```

and 

```
private void handlePhoneControllerEvents()
```

 to process game controller, and  phone controller key actions, and pass the processed String data to 
`protected abstract void processControllerKeyData(String data)` which is exposed to inheriting classes, which can be used to update the UI accordingly. 



#### `CameraFragment.java`

This class uses CameraX API to enable camera preview in the app. It extends the functionality of `ControlsFragment.java` to support robot connection and integration by default. 

To add camera preview to your feature:

1. Replace `extends Fragment`  with `extends CameraFragment` like this:

   ​	`public class AIFragment extends CameraFragment`
   
2. To overlay your own UI over this camera preview, `CameraFragment.java` offers two protected methods.

   ​	a.) `View inflateFragment(ViewBinding viewBinding, LayoutInflater inflater, ViewGroup container)`
   ​	b.) `View inflateFragment(int resId, LayoutInflater inflater, ViewGroup container)`

    Depending on whether you use ViewBinding or resource file to inflate your view, use the relevant method, and in the `onCreateView` method of your fragment, return it like this:

    ```
    @Override
    public View onCreateView(
        @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      binding = FragmentAiBinding.inflate(inflater, container, false);
    
      return inflateFragment(binding, inflater, container);
    }
    ```
    or
    ```
     @Override
     public View onCreateView(
         @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_ai, container, false);
        
        return inflateFragment(mView, inflater, container);
     }
    ```
## To add a new feature:

1. Create a new fragment and it's respective layout file.

2. Depending on whether camera preview is needed or not, extend either of `ControlsFragment.java` or `CameraFragment.java` class.

3. Based on the category and subcategory this feature falls in, add it in `FeatureList.java`, along with it's title, icon and color. Here's an example for the same.

   ```
   ArrayList<SubCategory> subCategories = new ArrayList<>();
   subCategories.add(new SubCategory(DEFAULT, R.drawable.openbot_icon, "#4B7BFF"));
   subCategories.add(new SubCategory(FREE_ROAM, R.drawable.ic_game, "#FFFF6D00"));
   subCategories.add(new SubCategory(DATA_COLLECTION, R.drawable.ic_storage, "#93C47D"));
   subCategories.add(new SubCategory(AI_MODELS, R.drawable.ic_person_search, "#FFD966"));
   categories.add(new Category(ALL, subCategories));
   ```

3. In `nav_graph.xml`, using the Design Editor, add your fragment to the graph, and link in with `mainFragment`.

4. Finally, inside switch block in `onItemClick` method of `MainFragment.java` , add your feature title as a new case and navigate to the screen using it's action id like this.

   ```
   Navigation.findNavController(requireView())
       .navigate(R.id.action_mainFragment_to_AIFragment);
   ```

