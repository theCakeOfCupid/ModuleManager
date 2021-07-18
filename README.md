# ModuleManager

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
        mavenLocal()
    }
    dependencies {
    	//引入插件
        classpath "com.james:module-manager-plugin:1.0.0"
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

    modulelibraryA(
          ...
    )
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

  指定仓库发布，当该属性被定义时，module将会发布到指定的仓库，且从该从库引入依赖件，例如示例清单中定义了mavenUrl = "$rootDir/moduleRepo"，则通过该插件发布的module会被发布到此目录下。若没有配置mavenUrl，将会默认发布到mavenLocal。



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

