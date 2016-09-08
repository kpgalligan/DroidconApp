//
//  AppDelegate.m
//  ios
//
//  Created by Kevin Galligan on 2/23/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

#import "AppDelegate.h"
#import "ViewController.h"
#import "co/touchlab/droidconandroid/presenter/AppManager.h"
#import "co/touchlab/droidconandroid/ios/IosPlatformClient.h"
#import "co/touchlab/droidconandroid/tasks/persisted/RefreshScheduleData.h"
#import "android/content/IOSContext.h"
#import "android/os/Looper.h"
#import "UIViewController+Utils.h"
#import <Fabric/Fabric.h>
#import <Crashlytics/Crashlytics.h>
#import "Reachability.h"
@import Firebase;


@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    [FIRApp configure];
    [Fabric with:@[[Crashlytics class]]];

    NSError* configureError;
    [[GGLContext sharedInstance] configureWithError: &configureError];
    NSAssert(!configureError, @"Error configuring Google services: %@", configureError);
    
    [GIDSignIn sharedInstance].delegate = self;
    
    [AndroidOsLooper prepareMainLooper];
    
    DCIosPlatformClient* platformClient = [[DCIosPlatformClient alloc] initWithDCIosFirebase:self];
    
    [DCPAppManager initContextWithAndroidContentContext:[AndroidContentIOSContext new]
                         withDCPPlatformClient:platformClient
                         withDCPAppManager_LoadDataSeed:self];
    
    Reachability *reachability = [Reachability reachabilityWithHostname:[[NSURL URLWithString:[[DCPAppManager getPlatformClient] baseUrl]] host]];
    
    reachability.reachableBlock = ^(Reachability *reachability) {
        NSLog(@"Network is reachable.");
    };
    
    reachability.unreachableBlock = ^(Reachability *reachability) {
        NSLog(@"Network is unreachable.");
    };
    
    // Start Monitoring
    [reachability startNotifier];
    
    // Register for remote notifications
    UIUserNotificationType allNotificationTypes =
    (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
    UIUserNotificationSettings *settings =
    [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    [[UIApplication sharedApplication] registerForRemoteNotifications];

    return YES;
}

- (BOOL)application:(UIApplication *)app
            openURL:(NSURL *)url
            options:(NSDictionary *)options {
    return [[GIDSignIn sharedInstance] handleURL:url
                               sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]
                                      annotation:options[UIApplicationOpenURLOptionsAnnotationKey]];
}

- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation {
    return [[GIDSignIn sharedInstance] handleURL:url
                               sourceApplication:sourceApplication
                                      annotation:annotation];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    NSString* type = userInfo[@"type"];
    //check the type. event messages just open the app so dont need to the handled here.
    if([type isEqualToString:@"updateSchedule"])
    {
        [CoTouchlabDroidconandroidTasksPersistedRefreshScheduleData callMeWithAndroidContentContext:[AndroidContentIOSContext new]];
        completionHandler(UIBackgroundFetchResultNewData);
    }
    else
    {
        completionHandler(UIBackgroundFetchResultNoData);
    }
}

- (void)signIn:(GIDSignIn *)signIn
didDisconnectWithUser:(GIDGoogleUser *)user
     withError:(NSError *)error {
    // Perform any operations when the user disconnects from app here.
    // ...
}

- (void)signIn:(GIDSignIn *)signIn
didSignInForUser:(GIDGoogleUser *)user
     withError:(NSError *)error {
    // Perform any operations on signed in user here.
//    NSString *userId = user.userID;                  // For client-side use only!
//    NSString *idToken = user.authentication.idToken; // Safe to send to the server
//    NSString *fullName = user.profile.name;
    
//    NSString *givenName = user.profile.givenName;
//    NSString *familyName = user.profile.familyName;
//    NSString *email = user.profile.email;
    
    LoginViewController *rootViewController = (LoginViewController *)[UIViewController currentViewController:[LoginViewController class]];
    [rootViewController loggedIn:user];
//    UIWindow *window = [UIApplication sharedApplication].keyWindow;
//    LoginViewController *rootViewController = (LoginViewController *)window.rootViewController;
//    [rootViewController loggedIn:user];
    // ...
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    [[FIRMessaging messaging] disconnect];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
    [self connectToFcm];
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

- (void)connectToFcm {
    [[FIRMessaging messaging] connectWithCompletion:^(NSError * _Nullable error) {
        if (error != nil) {
            NSLog(@"Unable to connect to FCM. %@", error);
        } else {
            NSLog(@"Connected to FCM.");
        }
    }];
}

- (NSString *)dataSeed
{
    NSString *fileName = [[NSBundle mainBundle] pathForResource:@"dataseed"
                                                         ofType:@"json"
                                                    inDirectory:@"dataseeds"];
    
    //check file exists
    if (fileName) {
        //retrieve file content
        NSData *partyData = [[NSData alloc] initWithContentsOfFile:fileName];
        
        NSString *myString = [[NSString alloc] initWithData:partyData encoding:NSUTF8StringEncoding];
        
        return myString;
    }
    else {
        NSLog(@"Couldn't find file!");
        return nil;
    }
}

- (void)logFirebaseNativeWithNSString:(NSString *)s{
    FIRCrashLog(s);
}

- (void)logPushFirebaseNativeWithNSString:(NSString *)s{
//    CLS_LOG(@"%@", s);
//    NSError *error = [NSError errorWithDomain:@"droidcon" code:200 userInfo:@{@"Error reason": @"Wrap Java Error"}];
//    [CrashlyticsKit recordError:error];
    FIRCrashLog(s);
//    assert(NO);
}

- (void)logEventWithNSString:(NSString *)name
           withNSStringArray:(IOSObjectArray *)params{
    NSMutableDictionary *dict = [[NSMutableDictionary alloc]initWithCapacity:[params length]/2];
    
    for(int i=0; i<[params length]; )
    {
        [dict setObject:[params objectAtIndex:i+1] forKey:(NSString*)[params objectAtIndex:i]];
        i = i+2;
    }
    
    [FIRAnalytics logEventWithName:name parameters:dict];
    
}

@end
