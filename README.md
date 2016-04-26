# FlexTag
FlexTag: A Highly Flexible PoS Tagging Framework

FlexTag is a PoS tagger which exposes the feature space to the user enabling him/her to fully customize the feature space.
Models that perform satisfiyling are furthermore easily persisted and can be used as standalone component.

*Target Audience:*
Everyone who feels that the existing, re-trainable PoS taggers such as Stanford or TreeTagger do not provide the needed flexiblity i.e. allow modifiyng the feature space but neither feels like implementing an own PoS tagger should consider `FlexTag`.

*Quickstart:*
FlexTag offers everything for creating and own PoS tagger model and evaluating it on any input data format. Additional to training, persisting of own models the trained model is easily evaluate by using out-of-the-box available evaluation methods such as `Train-Test` or `N-fold CrossValidation` on any data set.

All functionalities are provided in the example project for an easy start:
In the `example` project you find a runnable demonstration of using train/test, cross-validation and model persisting with FlexTag if you take a look into the following demos:
  * ExampleCrossValidation
  * ExampleTrainTest
  * ExampleTrainStore

FlexTag uses DKPro TextClassification in the background and provides additionally file format readers for many formats commonly used in NLP, see `ExampleReaders` for a pointer to learn more about the supported file formats. Furthermore, the machine learning algorithm used for training a model is exchangeable, too. Users can choose from `CrfSuite, SvmHmm and the classifier provided by Weka` for training and evaluating their model, see package `de.unidue.ltl.flextag.examples.config` for changing the classifier.

**Features**

[Adding of user-defined features](https://github.com/Horsmann/FlexTag/wiki/Adding-user-defined-features)

[Limitations of user-defined features](https://github.com/Horsmann/FlexTag/wiki/Limitations-of-user-defined-features)

