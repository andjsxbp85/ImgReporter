# ImgReporter
:art: This library will help you to manage image from serenity report (auto arranger and compressor image then stored it into a new floder in your Project folder). In next version will be update in Testlink Auto Attachment for your Test Case Execution

## HOW TO USE
**Change maximum image size variable to your desired value (in KB):**
>int maxImgSize = 70;
* max image size each step in KiloBytes
* write '0' to set default value (default value = 80 KB, max image size) 
* Min of maxImgSize is 10, if u set under 10, it will be automatically set to 10
* Your image will be automatically arranged and stored in 'ScreenShots' folder in your test project
```
I use Intelij Idea as IDE , Please tell me the bugs, so I can fix it soon
```


#### In this version you can also upload the compressed image results semi-automate by changing this variable:
> int execID = 37191;
> change 37191 to your last execution ID



## Next Version

> Set the latest execution ID of your test case, but im in exploration step to know the ways to upload image to testlink automatically without set execution ID.
**To Do List: **
- [x] Upload in test case semi automate
- [ ] Upload image to test case fully automated


## Contributors and Helper
:sweat_drops: Credit:
> Anjas Muhammad Bangun

:kissing_heart: Special Thanks:
> - Allah SWT
> - Billy Julius
> - Bruno P. Kinoshita
> - Sepulsa and Alterra
