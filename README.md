# Processor Resource Manager

_This component was created as an output of the INCISIVE European project software, forming part of the final platform_

### Introduction
The Processor Resource Manager (PRM) is the component in charge of decoupling the platform aspects from the AI Engine, providing simple standards instead. The main idea is to  make the AI Researcher not to worry about performing requests to the multiple components  of the platform with their different characteristics and their possible future updates. In this way, the AI Researcher can focus on developing the AI logic and maintain an implementation the more agnostic about the INCISIVE platform as possible.

Check the last version of the D.3.X report for the full abstract description of the component and their functionalities.

### Implementation
The component is implemented in Java with the help of the Maven package manager. The idea behind the design of the component implementation is very simple. It implements a set of possible actions to run in the platform. The actions to perform and the specific parameters' values for them are specified as an input argument with a JSON array.

### How to set up
The component can be build with Maven or any modern IDE. Nevertheless, the instructions described here show how to build the component with docker. 

It is only necessary to build the docker image with the provided Dockerfile i.e. `docker build -f Dockerfile -t processor-resource-manager .`

### How to run
When running the component with docker is only necessary to provide as arguments a JSON containing an array with the actions to perform. The required information and syntax for all possible actions can be retrieved from the directory located at `src/test/resources/input_configurations`. 

As an example, the action of creating a directory has the following syntax:
```
{
  "name": "create_directory",
  "directory_path": "src/test/resources/tmp_create_directory_tests/test/some_dir_1/some_dir_2"
}
```

On the other hand, as another example, the action to run an AI Engine looks as follows:
```
{
  "name": "run_ai_engine",
  "use_case": "training_from_scratch",
  "max_iteration_time": 30,
  "max_initialization_time": 2,
  "max_finalization_time": 5,
  "max_finalization_retries": 3,
  "client_host": "127.0.0.1:8001",
  "server_host": "127.0.0.1:8000",
  "ping_url": "/api/ping",
  "run_url": "/api/run",
  "end_url": "/api/end",
  "callback_url": "/api/callback"
}
```

In the case, that we would like to run both actions, we should provide the following structure as an input argument:
```
{
  "actions": [
    {
      "name": "create_directory",
      "directory_path": "src/test/resources/tmp_create_directory_tests/test/some_dir_1/some_dir_2"
    },
    {
      "name": "run_ai_engine",
      "use_case": "training_from_scratch",
      "max_iteration_time": 30,
      "max_initialization_time": 2,
      "max_finalization_time": 5,
      "max_finalization_retries": 3,
      "client_host": "127.0.0.1:8001",
      "server_host": "127.0.0.1:8000",
      "ping_url": "/api/ping",
      "run_url": "/api/run",
      "end_url": "/api/end",
      "callback_url": "/api/callback"
    }
  ]
}
```

The correct way to provide the previous JSON contents to the docker container is as follows:
```
docker run -it processor-resource-manager '{ "actions": [ { "name": "create_directory", "directory_path": "src/test/resources/tmp_create_directory_tests/test/some_dir_1/some_dir_2" }, { "name": "run_ai_engine", "use_case": "training_from_scratch", "max_iteration_time": 30, "max_initialization_time": 2, "max_finalization_time": 5, "max_finalization_retries": 3, "client_host": "127.0.0.1:8001", "server_host": "127.0.0.1:8000", "ping_url": "/api/ping", "run_url": "/api/run", "end_url": "/api/end", "callback_url": "/api/callback" } ] }'
```

As an optional parameter, there is the possibility to provide a URL of an API. The component will hit this URL in case of an error. The correct way to provide this optional parameter is as follows:
```
docker run -it processor-resource-manager '[...]' --failure-endpoint http://HOSTNAME:PORT/PATH_TO_HIT_IN_CASE_OF_ERROR
```