#Contributing to Riemann

Please see the [Contributing to Riemann](http://riemann.io/howto.html#contributing-to-riemann) section of the howto at Riemann.io.

Subsections of howto:

* [Write a client](http://riemann.io/howto.html#write-a-client)
* [Work with the Riemann Source](http://riemann.io/howto.html#work-with-the-riemann-source)
* [Building riemann-java-client](http://riemann.io/howto.html#riemann-java-client)
* [Fix a bug or add a feature](http://riemann.io/howto.html#fix-a-bug-or-add-a-feature)
* [Help write documentation](http://riemann.io/howto.html#help-write-documentation)

# Working with the Riemann Source

I try to keep master as clean and runnable as possible. Riemann has an exhaustive test suite, which helps ensure code quality. If you plan on changing the Riemann source, fork it on Github so you'll be able to send pull requests quickly. If you just want to run the latest version, go ahead and clone the official repo.

Cloning official repo:

    git clone git://github.com/aphyr/riemann.git
    cd riemann

You'll also need a JVM, and leiningen 2--the Clojure build system.  [Leiningen 2 installation instructions](https://github.com/technomancy/leiningen#installation)

## Leiningen commands:

### Run the suite of tests:

    lein test

    #run optional tests requiring installed services
    lein test :graphite
    lein test :email

    #run test for a single namespace only
    lein test riemann.test.streams

### Start Riemann:

    lein run

Riemann will read the file `riemann.config` in the current directory. If you want to run a different config file, try:

    lein run -- path/to/my/riemann.config

### Build a fat jar:

    lein uberjar

This builds target/riemann-{version}-STANDALONE.jar.  Copy this jar as needed.

### Build tarball, debian packages, and md5sums:

    lein pkg

.debs and .tar.gz files, plus md5sums, will appear in target/

## protocol buffer and clojure client

The protocol buffer codec and clojure client live in riemann-clojure-client, which wraps the java protobuf code and java client in riemann-java-client. Both of these are available on clojars and most of the time you can ignore them.

However, if you need to change the protocol or client, you can fork these projects and make your changes there.  Github projects for [protocol buffer codec](https://github.com/flatland/clojure-protobuf) and [clojure client](https://github.com/aphyr/riemann-clojure-client)

## Building `riemann-java-client`

You'll need maven, and the protocol buffers compiler (protoc) version 2.4.1.

When you've made changes to the java client, install it with `mvn install`; then test the clojure client and install it with `lein install`. Finally, you can run riemann itself. You may need to check that the client versions you're working with match up in the riemann and riemann-clojure-clientproject.clj files.

# Fix a bug or add a feature

First, fork [Riemann](https://github.com/aphyr/riemann) on github. Clone your fork and create a new topic branch for your fix:

    git clone git@github.com:your-github-username/riemann.git
    cd riemann
    git checkout -b fix-some-bug

Most of Riemann's source lives in src/riemann/. Corresponding tests live in test/riemann/test/. When you fix a bug or add a feature, make sure to add new tests that confirm its correctness!

You can run the test suite with:

    lein test

    #run optional tests requiring installed services
    lein test :graphite
    lein test :email

    #run test for a single namespace only
    lein test riemann.test.streams

Some tests for integrating with other services require a local sendmail, or graphite, or credentials for a web service. If you make changes that affect these systems, you can test them with special selectors like lein test :graphite or lein test :email. If you're working with a particular namespace, like riemann.streams, lein test riemann.test.streams runs only the tests for that namespace.

Once your tests pass, commit your changes and push them to github:

    git commit -a
    git push origin fix-some-bug

If you change more than a few lines of whitespace, please make your formatting changes in a separate commit; it'll be easier for me to read and understand your changes. Please try to send me only a few commits where possible; use git rebase --interactive to squash your small changes:

    git rebase --interactive origin/master

Now file a pull request.  [Github pull request documentation](https://help.github.com/articles/using-pull-requests)

Thank you!

# Help write documentation

Riemann's web site and documentation are in the `gh-pages` branch of the riemann repository. Fork riemann on github, clone your fork, and check out the branch:

    git clone git@github.com:your-github-username/riemann.git
    cd riemann
    git checkout gh-pages
    vim howto.html

Pages are built with Jekyll. To see how your changes will appear on the site,
run jekyll and open _site/howto.html in a web browser:

    sudo apt-get install python-pygments jekyll
    cd riemann
    jekyll
    # _site/howto.html has been updated

When you're satisfied with your changes, commit, push, and send me a pull request:

    git commit -am "Added a howto guide for integrating with FooService"
    git push

Now file a pull request.  [Github pull request documentation](https://help.github.com/articles/using-pull-requests)

Thank you!
