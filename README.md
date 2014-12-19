Easy-LDAP-Connection-Java-API
=============================

## Introduction
This is an easy LDAP connection Java API,
providing a basic database interface.

- Create a new user
- Get an user
- Update user fields
- Remove an user
- Custom filtering

It was my intention not to implement everything, I just wanted a very simple and very user-friendly API.
Of course if you find something very important just send me a mail or fork this project and i will add it.

##Usage

#Instantiation
```java
try {
         connection = new LDAPConnection(Settings.DB_HOST,
                                         Settings.DB_USER_DN,
                                         Settings.DB_LOGIN,
                                         Settings.DB_PASSWORD);
        } catch (AuthenticationException e) {
            System.err.println("LDAP -> Wrong Authentication");
        } catch (NamingException e) {
            System.err.println("LDAP -> Couldn't connect: " + e.getMessage());
            e.printStackTrace();
        }
```
You can, if you need, specify another port. Default: 389

#Get an user
```java
SearchResult result = connection.getUser(username);

try {
    String uid = result.getAttributes().get("uid").get(0).toString();
    String displayName = result.getAttributes().get("displayName").get(0).toString();
    String mail = result.getAttributes().get("mail").get(0).toString());

    return new Student(uid,displayName,mail);

} catch (NamingException e) {
    return null;
}
```

#Update user fields
```java
ModificationItem[] mods = new ModificationItem[2];
mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("displayName", student.getDisplayName()));
mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("mail", "'"+student.getPrivateMail()));

connection.updateUser(student.getUID(), mods);
```
You can also DirContext.ADD_ATTRIBUTE and DirContext.REMOVE_ATTRIBUTE

# There are more methods but I think they are easy to understand, but don't hesitate to contact with questions

##License
The MIT License (MIT)

Copyright (c) 2014 Patrick Stillhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
