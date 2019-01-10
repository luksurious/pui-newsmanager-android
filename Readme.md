# Android application for the News Manager project

for 'Programming of User Interface'

## Requirements

- Android SDK version 28

## Setup
*Note: this is not required for the submitted application*

- Create a copy of `app/src/main/res/raw/config_sample.properties` and name it `config.properties`.
  Fill in the template to be able to connect to the server.

## Implementation details

**Provided code**
* The provided code is in the namespace `es.upm.hcid.newsmanager.assignment`
* All changes to this code are commented and annotated with `@author students`
* Notable changes include:
  * The `ModelManager` supports the anonymous group API key, and does not log in in the constructor
  * `Logout` function is added
  * The thumbnail conversion error is detected and handled separately
  

**Activities and UI**
* Activities are independent, all initialize the `MainPreferences` and use the `ServiceFactory` to get a connection to the server
* All strings, colors, sizes, etc. are extracted into the corresponding resource files


**Special features**
* Opening the detail activity uses a custom animation, the activity slides in from the right
  * Going back reverses this animation
  * See the `res/anim` folder and the usages of these files
* The article detail page has a collapsible toolbar, with the main image displayed
  * To make the text easier readable, a text scrim (semi-transparent gradient) is added behind the text
* To select the source of the image to update, a `BottomSheetDialogFragment` is used