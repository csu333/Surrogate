# Surrogate
Surrogate is a Xposed module to replace any method, sending constant instead. If the main purpose of this software is to act as a complement for [RootCloak](https://github.com/devadvance/rootcloak), it could probably be used to mock services.

## Requirement
Surrogate is a Xposed module an thus needs [Xposed Framework](https://forum.xda-developers.com/showthread.php?t=3034811) installed. As such, Surrogate doesn't need your phone to be rooted but of course, if it isn't, you probably won't be able to install the Xposed Framework.

## Usage
### Basic
If you don't feel like reverse engineering application, you don't need to. You can use the pre-defined rules. These can be imported locally or from internet (see the menu of the application). The internet version might support more application but could not be supported by your version if you don't use the last beta.

Currently supported application are (thanks to Razer2015):
 * [Bancontact](https://play.google.com/store/apps/details?id=mobi.inthepocket.bcmc.bancontact)
 * [MyBank Belgium](https://play.google.com/store/apps/details?id=com.db.pbc.mybankbelgium)
 * [BNP Paribas Fortis Prepaid](https://play.google.com/store/apps/details?id=be.bnpparibasfortis.bnppfprepaid)
 * [Nordea Codes](https://play.google.com/store/apps/details?id=com.nordea.mobiletoken)
 * [Nordea Pay](https://play.google.com/store/apps/details?id=fi.nordea.mep.npay)
 * [Twyp Cash](https://play.google.com/store/apps/details?id=es.ingdirect.twypcash)
 * [NAB](https://play.google.com/store/apps/details?id=au.com.nab.mobile) (Only with beta version)
 * [Keytrade Bank](https://play.google.com/store/apps/details?id=be.keytradebank.phone) (tentative)
 
 If you find the application you want to fool in this list, just enable the corresponding set of rule and make sure the application is completely stopped (killed). When you restart it, there should be no complaining about your rooted phone anymore.
 
### Advanced
You your application is not listed here, I would first suggest you to have a look at [RootCloak](https://github.com/devadvance/rootcloak) which could solve your issue. If it doesn't, you can create a hook using Surrogate to fool the application. This will require some effort to find out which method to hook. This is the biggest challenge. If you don't know where to start, here are some pointers:
 * [Attacking Android Applications With Debuggers](https://blog.netspi.com/attacking-android-applications-with-debuggers/)
 * [jadx decompiler](https://github.com/skylot/jadx)
 
So now you the the (many) methods that are called in order to check if your phone is rooted ! All you need to do now is to create a rule for it. First, create a "Package". This is the one of the targeted application (if you have any doubt, this is also the "id" parameter you find in the URL of the application in the Play Store.

In this package, you have to add one rule for each method to hook. A rule needs 4 pieces of information
 1. The class name: This is the full class name (package + class name)
 2. The name of the method (case sensitive, of course)
 3. The return type of the method. Once again, it is the full class name or type which is needed. For your convenience, I've defined a few that you can use:
   * Boolean
   * Integer (which could be int instead but thanks to autoboxing, this works just as well)
   * String
   
   For the most demanding application (and only from the 1.1 version), you can create a beforeHookedMethod or afterHookedMethod method. In the next field, you can give the Java body of your method. I use [BeanShell](http://www.beanshell.org/docs.html) to enable on-the-fly compilation of the method. Basically, you should be able to do anything you want but (and this is a big "but") the number of objects available at run time is pretty limited. You can count on 2 variables that are in the scope of the method: 
     * context which is ... well the context object. Once again, this is the Xposed framework context so it is pretty limited 
     * param which is the parameter of the method
 Custom allows you to do any kind of computation (with the same limitation than above) but must return an object which will be returned to the hooked method. To return an object from a BeanShell script, you don't need to do anything special: the last line gives the result (see also [here](http://www.beanshell.org/manual/embeddedmode.html#eval()))
 4. The value to return. If this is Integer, Boolean or String, this is pretty much self-explanatory. You don't need escaping or anything. Please note that there is no check on this value (if you put "true" but select Integer in the type, this will crash at runtime, rendering Surrogate useless).
 5. If the method you hook has a parameter (or more than one), indicate here all the parameter (fully qualified) type, one on each line.
 
Please note that if the method you want to hook is overloaded, you must define one rule for each set of parameter.

If you go through all this trouble, please send me your rule set so I can add it to the supported applications.

# Logging
Almost all logging is done in logcat.

## Credits
* To [rovo89 and all the contributors](https://github.com/rovo89/) for their amazing work on Xposed framework
* To [Razer2015](https://github.com/Razer2015/) for the inspiration and the reverse engineering done on some apps. Most of the rules existing in the default set are from him.

