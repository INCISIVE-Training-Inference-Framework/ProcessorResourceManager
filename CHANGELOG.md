## v2.2.0 (2024-02-29)

*  upd: updates behaviour when uploading multiple evaluation metrics [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/c551a6895409ce59af7286cb2cd7dd6dc7bc3667)
*  updates prepare internal data action to retrieve data partner information from an url instead than from the input json [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/b6cfdaa0fb1189b2d881adcaf35c15d1c2c69154)
*  fix: solves critical bug in action prepareInternalData. When more than patient one existed, at the end only the last one was present overwriting the previous [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/9498504065ee8cc91dc86219123f78987f481aec)
*  fix: fixed tests [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/3878c58895986c50dc614707c6e96fa046dbf0c8)
*  upd: refactors tests paths [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/a548b9de69b5f92ee252ff73d464a1565639917c)
*  merges with master [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/05627b4a0fdceb1654a4b76c4584ebb576eb5243)
*  feat: add report_metadata feature [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/28f75eed938326bb180404fae51599dabda5b90f)
*  upd: add docker-compose.yml [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/3dcca4092febe9b1a48ec9da59744dbcf9e5eb05)


## v2.1.2 (2023-07-31)

*  adds the lung, prostate and colorectal cancers to the PrepareInternalData action [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/61e0d88f38ae01dbe8c8d0e8c6c5238b6cc4b27b)
*  refactors actions implementation into different files for better readibility [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/39073592fd29d806b297308320ddba957be3d3d7)
*  upd: refactors the zip behaviour, zip root contents are written down directly, no need for external folder anymore, also the symbol * can be included to zip the contents without including the root folder [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/06f722b2c8b0f64001c3643227cc0c67b69e0e49)
*  refactors error showing in RunAIEngineAction when the AI Engine fails during the use case execution [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/e2cb7651d311e3e153e7501c5cc850d2675b7e08)
*  cleans error message stored into file of strange symbols that could cause a parsing error later on [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/2e71613fe25c7ba7e8d45be715e94bbc31e31236)
*  feat: adds pingAIEngine actions [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/63a0376f480dd8c97e4b32b21dc3f66d3a938cc4)
*  upd: only cleans " and \' symbols from stored error message [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/27107bcd7aed455c664d0b141b85a8b722449331)
*  fix: solves memory leakage when uploading files in action UpdateToSucceeded [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/adf4cb46d5932104ff37497f38da09a3be19bc80)
*  upd: updates download actions to give the option to modify the number of retries to resume the download from the external server [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/4ab1d75b9823fcbb30ebae69c35739bf63a2fc69)


## v2.1.1 (2023-07-19)

*  fix: solves bug with zip uncompression [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/76897e627005738a2eb9777a2bacc0eac46f9fa2)


## v2.1.0 (2023-07-18)

*  adds the option to resume downloads of files [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/911e103ec7f73567bc6adadaaa237f50762acc15)
*  performs all required changes to be able to run functionalities with the internal data of the federated nodes [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/81a53e29c12b108fc3c910f1e2388050dc64e66b)


## v2.0.5 (2023-05-09)

*  heavily reduces memory usage in downloadAIModel and downloadExternalData actions with the help of a temporary file that is deleted after the unzipping is finished [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/aa0715e6abe5dd7366fbf65538a836624f44fa7d)
*  adds exception handling to own server host shutting down [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/133b3c67ad32f0f4538391a884610ae6a0f613c9)
*  adds action addDataProviderInfo [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/3fb84a77b4bc9c30dce982afb53eeae0c96fad7e)


## v2.0.4 (2023-04-28)

*  adds action to change the platform vars api host and port [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/b87cbde7a8e0185bcf17b404a42a85e0f1a6ad76)
*  updates readme [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/c12a796548bacd6e67c364d7db990cde0ce5066d)
*  modifies run_ai_engine action to finish the AI Engine after the running of the use case through a new endpoint [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/fc3dd29d39ebc7e2dc89acadd89f6bd739a006a4)
*  updates readme with changes in run_ai_engine action [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/96bc2b2dd2da8f5bf7931dffb502d457848a2ee8)
*  moves ai engine ending to a new action instead of the running one, adds the possiblity to run an array actions in case of failure [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/5406d7618b52ce343dea6233b0b1df69d4f6df55)


## v2.0.3 (2023-04-18)

*  adds evaluationMetricMultiple upload action [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/434b38f972336bfe19e88e2aef2359a768954ff6)


## v2.0.2 (2023-04-11)

*  unables the option of uploading evaluation metrics without an ai model [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/aa1472f803d10030fffd176e85e039a6a6d4084c)


## v2.0.1 (2023-03-28)

*  updates update_to_failed action to include error message in input json [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/67b4d147b42a04dcc5f751475858ef889a73421c)
*  adds writing of error message to text file in case of failure [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/f6254554c79a1bdd2dff7dd2865d08574470633f)
*  updates update_to_failed action tests [View](git@gitlab-internal.bsc.es:datacentric-computing/incisive-project/incisive_platform_processor_resource_manager.git/commit/fb3957663e77e5f68ccc351097651877c2544b30)


