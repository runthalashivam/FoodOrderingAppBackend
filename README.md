## How to Run

* Clone the repo
```
git clone https://github.com/runthalashivam/FoodOrderingAppBackend
```
* Open the project in Intellij and create configuration for to run the App with following parma - 
    * **Name** - FoodOrderingAppApiApplication

    * **Main Class** - com.upgrad.FoodOrderingApp.api.FoodOrderingAppApiApplication

    * **Working Directory** - <path_to_FoodOrderingAppBackend>

    * **Module** - FoodOrderingApp-api

* Import dependecies by clicking on the Reimport icon on maven sidebar tab in Intellij

* Update the PostgresDB URL, ussername & password details in - *FoodOrderingApp-api/src/main/resources/application.yaml*

* Run the command - `mvn clean install -Psetup -DskipTests`

* Run the `FoodOrderingAppApiApplication` configuration
