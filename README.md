# FlexTag
FlexTag: A Highly Flexible PoS Tagging Framework

FlexTag is a PoS tagger which exposes the feature space to the researcher enabling him/her to fully customize the feature space to be used for training a model.
FlexTag ships with a built-in functionality to perform train/test and cross-validation on a given set of training data, enabling it to easily evaluate new feature configurations.
Models that perform satisfiyling are furthermore easily persisted and can be used as standalone component.

*Publication*
FlexTag is described in detail here:
Zesch, Torsten; Horsmann, Tobias, FlexTag: A Highly Flexible Pos Tagging Framework (Inproceeding), 10th edition of the Language Resources and Evaluation Conference, European Language Resources Association (ELRA), Porovoz, Slovenia, 2016. 

*Quickstart:*
In the `example` project you find a runnable demonstration of using train/test, cross-validation and model persisting with FlexTag
  * ExampleCrossValidation
  * ExampleTrainTest
  * ExampleTrainStore

FlexTag uses DKPro TextClassification in the background and provides additionally file format readers for many formats commonly used in NLP, see `ExampleReaders` for a pointer to learn more about the supported file formats. Furthermore, the machine learning algorithm used for training a model is exchangeable, too. Users can choose from `CrfSuite, SvmHmm and the classifier provided by Weka` for training and evaluating their model, see package `de.unidue.ltl.flextag.examples.config` for changing the classifier.

**Features**

[Adding of user-defined features](https://github.com/Horsmann/FlexTag/wiki/Adding-user-defined-features)

[Limitations of user-defined features](https://github.com/Horsmann/FlexTag/wiki/Limitations-of-user-defined-features)

