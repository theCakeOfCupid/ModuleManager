



# ModuleManager

[TOC]

[![](https://jitpack.io/v/theCakeOfCupid/ModuleManager.svg)](https://jitpack.io/#theCakeOfCupid/ModuleManager)

## 支持功能

- 自动构建发布脚本
- 动态切换依赖
- 一键发布所有module至仓库
- 动态切换maven仓库



## 完成配置只需要两步

### 1、引入插件

在工程目录下build.gradle文件中引入插件

```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply plugin: 'module-manager-plugin'

buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
    	//引入插件
        classpath 'com.github.theCakeOfCupid:ModuleManager:1.0.2'
    }
}
```

### 2、配置module清单

你可以在工程目录下build.gradle文件里直接配置也可以单独创建一份清单文件，然后在工程目录下build.gradle文件里引入，

我这里的示例是第二种，这里我在工程目录下创建了一个module-settings.gradle的文件，具体配置如下：

```
moduleSettings {
    libraryA(
            useByAar: false,
            groupId: 'com.james',
            artifactId: 'libraryA',
            version: '1.3'
    )
    libraryB(
            useByAar: false,
            groupId: 'com.james',
            artifactId: 'libraryB',
            version: '1.2'
    )
	
    mavenUrl = "$rootDir/moduleRepo"
}
```

然后在工程目录下build.gradle文件里引入即可

```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply plugin: 'module-manager-plugin'
apply from: 'module-settings.gradle'
```

#### 配置参数说明

- **libraryA、libraryB**

  显而易见是module名称，这里一定要和你的真实module名称一致，不然插件会找不到对应的module，如果你的module名携带特殊字符，你需要处理一下，因为groovy dsl对特殊字符支持并不友好，例如你的module名是module-library-A，你需要将特殊字符去掉，改成modulelibraryA，即

  ```
    modulelibraryA(
              useByAar: false,
              groupId: 'com.james',
              artifactId: 'module-library-A',
              version: '1.2'
    )
  ```
  
- **useByAar**

  是否以aar方式引入，默认为false，该属性被设置为true时，项目里所有以project方式引用此module的依赖会自动替换成aar引用

  比如app模块当前是以project方式引用的libraryA

  ```
  dependencies {
  	implementation project(path: ':libraryA')
  }
  ```

  当useByAar被设置成true时，该依赖会自动转换成aar依赖，无需手动更改。

- **groupId、artifactId、version**

  这个没啥好说的，发布到仓库的三件套，仓库通过这三个路径找到对应的产物。

- **mavenUrl**

  指定仓库发布，当该属性被定义时，module将会发布到指定的仓库，且从该从库引入依赖件，你可以单独为module设置自己的仓库，也可以设置一个全局仓库，也可以什么都不设置（默认将使用本地maven仓库），示例如下：
  
  ```
  moduleSettings {
      libraryA(
              useByAar: true,
              groupId: 'com.james',
              artifactId: 'libraryA',
              version: '1.4'
      )
      libraryB(
              useByAar: true,
              groupId: 'com.james',
              artifactId: 'libraryB',
              version: '1.2',
              //为libraryB单独设置仓库
              mavenUrl:"$rootDir/moduleRepoB"
      )
  	//设置全局仓库
      mavenUrl = "$rootDir/moduleRepo"
  }
  ```
  
  仓库生效优先级依次是：module参数内配置的仓库 > 全局仓库 > 本地仓库

## 一键发布所有module

```
gradlew oneKeyPublish
```



## 发布单个module

```
gradlew  :<module名称>:assembleRelease

//例如发布libraryA
gradlew  :libraryA:assembleRelease
```
## module内引用了本地aar怎么办

需要将本地aar继续转换成仓库，两步即可实现：

### 1、配置本地aar仓库

以demo为示例，我们在项目根目录下新建一个文件夹“localAarRepo”，将所有本地aar放入该文件夹，然后跟模块配置一样，我们新建一个aar-settings.gradle的配置文件：

```
localAarSettings{
	//存放本地aar的路径
    aarDir = "$rootDir/localAarRepo"
    //本地aar生成的仓库路径
    mavenUrl = "$rootDir/localAarRepo/localAarMavenRepo3"
}
```

同样，我们需要在项目build.gradle里引入aar-settings.gradle配置文件

```
//...
apply plugin:'module-manager-plugin'
apply from: 'module-settings.gradle'
apply from: 'aar-settings.gradle'
//...
```

完成以上步骤，我们就可以将所有本地aar发布到配置的仓库路径下了，发布命令为：

```
gradlew oneKeyPublishLocalAar
```

### 2、引用本地aar

在步骤一里面我们已经成功将所有本地aar发布到仓库，接下来我们只需要在对应的模块更改下引用方式即可，例如我们需要引用test1.aar：

```
//引用test1.aar
implementation aars["test1.aar"]
//引用test2.aar
implementation aars["test2.aar"]
```



## 直接引用仓库模块路径

当本地仓库已有module aar且settings.gradle文件下module没有配置时，直接引用aar
```
implementation moduleSettings.module('libraryA')
```