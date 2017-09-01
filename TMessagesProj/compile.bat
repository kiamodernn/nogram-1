@echo on
@set BASHPATH="C:\cygwin\bin\bash"
@set PROJECTDIR="/cygdrive/e/\momoGram/TMessagesProj"
@set NDKDIR="/cygdrive/d/ProgramInstaled/SdkAndroidStudio2/ndk-bundle/ndk-build.cmd"
%BASHPATH% --login -c "cd %PROJECTDIR% && %NDKDIR%
@pause:
