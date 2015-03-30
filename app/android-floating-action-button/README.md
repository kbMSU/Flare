FloatingActionButton
====================
Yet another library for drawing [Material Design promoted actions](http://www.google.com/design/spec/patterns/promoted-actions.html).

Features
========
* Support for normal `56dp` and mini `40dp` buttons.

  ![Demo](screenshots/buttons.png)

* Customizable background colors for normal and pressed states (only solid colors supported) and icon drawable.

  ![Demo](screenshots/custom.png)

* Convenience `AddFloatingActionButton` class with plus icon drawn in code.
* `FloatingActionsMenu` which can be expanded/collapsed to reveal multiple actions.

  ![Demo](screenshots/menu.gif)

Usage
=====
The library will be soon available on Maven Central, but for now you have to build it and install it in your local Maven repo:

```
git clone https://github.com/futuresimple/android-floating-action-button
cd android-floating-action-button
cd library
gradle installArchives
```

And add the following configuration to your `build.gradle`:

```groovy
dependencies {
    compile 'com.getbase:floatingactionbutton:1.1.0'
}
```

To see how the buttons are added to your xml layouts, check the sample project.

Caveats
=======
The API is **extremely** limited at the moment. It solves few select use cases in the app I'm working on.

Unlike some other FloatingActionButton libraries this library doesn't implement "quick return" pattern, i.e. hiding the button on scrolling down and showing it on scrolling up. That's intentional, I think that should be responsibility of another component, not the button itself.

Credits
=======
I used [FloatingActionButton](https://github.com/makovkastar/FloatingActionButton) library by [Oleksandr Melnykov](https://github.com/makovkastar) as a base for development.

License
=======

    Copyright (C) 2014 Jerzy Chalupski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.