### Approach

The project is controlled with 3 constants:
* ColdCommandMap
* HotCommandMap
* TemperatureCommandMap

The constants act as a config in code. The constant objects allow additional temperatures and clothing
options to be added somewhat painlessly. To add a new temperature, create a new TemperatureCommandMap and
add a key-value pair to the TemperatureCommandMap (e.g. "Balmy" -> BalmyCommandMap). 
The downstream functions are agnostic and only depend on the data objects input as parameters.

The CommandMaps (currently HOT and COLD) use prerequisite sets to handle the logic "x cannot be put on before y". 
This logic can be used with the terminal step, the terminal step is just a step with all other steps a prerequisites. 

Dependencies are passed as parameters to ease testing. 
This seems easier than using a dependency injection framework for such a small project.

### Testing
`sbt test`

Validator.scala has test coverage. Main.scala is used as an entry point and for printing (side-effect) so it isn't covered with tests. There's very little logic in the Main class.

### Run the project
`sbt compile`

`./get-dressed.sh COLD 8, 6, 3, 4, 2, 5, 1, 7`
