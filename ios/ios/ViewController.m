//
//  ViewController.m
//  ios
//
//  Created by Kevin Galligan on 2/23/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

#import "ViewController.h"
#import "co/touchlab/droidconandroid/presenter/ConferenceDataPresenter.h"
#import "co/touchlab/droidconandroid/presenter/ConferenceDayHolder.h"
#import "co/touchlab/droidconandroid/presenter/AppManager.h"
#import "co/touchlab/droidconandroid/data/Block.h"
#import "android/content/IOSContext.h"
#import "PlatformContext_iOS.h"
#import "java/util/ArrayList.h"
#import "java/util/List.h"
#import "NoteTableViewCell.h"
@import FirebaseMessaging;


@interface ViewController ()

@property (nonatomic, assign) long track;
@property (nonatomic, strong) NSMutableArray *notesArray;
@property (nonatomic, strong) NSMutableArray *imagesArray;
@property (nonatomic, strong) PlatformContext_iOS *platformContext;
@property (nonatomic, strong) DCPConferenceDataPresenter *dataPresenter;
@property (nonatomic, strong) JavaUtilArrayList *notes;
@end

BOOL allEvents = NO;

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if (self.tabBarController.selectedIndex == 0) {
        allEvents = YES;
        self.navigationItem.title = @"Droidcon NYC";
    } else {
        allEvents = NO;
        self.navigationItem.title = @"My Agenda";
    }

    // Hide the nav bar shadow
    [self.navigationController.navigationBar setShadowImage:[[UIImage alloc] init]];
    [self.navigationController.navigationBar setBackgroundImage:[[UIImage alloc]init] forBarMetrics:UIBarMetricsDefault];
    
    self.navigationController.navigationBar.translucent = false;
    
    [[FIRMessaging messaging] subscribeToTopic:@"/topics/all"];
    [[FIRMessaging messaging] subscribeToTopic:@"/topics/ios"];
}

- (void) viewWillAppear:(BOOL)animated
{
    // refresh every time it appears so that we see updates, doesn't seem to affect scroll position of list
    [self loadConferenceSchedule];
    self.tableView.tableHeaderView = nil;
    self.tableView.tableFooterView = nil;
    
    self.tableView.delegate = self.platformContext;
    self.tableView.dataSource = self.platformContext;
}

- (void)createSDASimple
{
    if(self.platformContext == nil)
    {
    PlatformContext_iOS *pcios = [PlatformContext_iOS new];
    self.platformContext = pcios;
    self.platformContext.reloadDelegate = self;
    self.dataPresenter = [[DCPConferenceDataPresenter alloc] initWithAndroidContentContext:[DCPAppManager getContext] withDCPConferenceDataHost:pcios withBoolean:allEvents];
    }
    else
    {
        [self.dataPresenter refreshConferenceData];
    }
}

- (IBAction)updateTable:(id)sender
{
    NSLog(@"Updating Table");
    if ([self.dayChooser selectedSegmentIndex] == 0) {
        self.platformContext.isDayTwo = NO;
    } else {
        self.platformContext.isDayTwo = YES;
    }
    [self.platformContext updateTableData];
    [self.tableView reloadData];
}

- (void)loadImageWithPath:(NSString *)imagePath
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                         NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *path = [documentsDirectory stringByAppendingPathComponent:imagePath];
    UIImage *image = [UIImage imageWithContentsOfFile:path];
    UIImageView *imgView = [[UIImageView alloc] initWithImage:image];
    [self.imagesArray addObject:imgView];
}

#pragma PlatformContext_iOS - Delegate
- (void)reloadTableView
{
    NSLog(@"Look, we're here");
    [self.tableView reloadData];
}

- (void)showEventDetailViewWithEvent:(DCDEvent *)event andIndex:(long)index
{
    self.track = index;
    [self performSegueWithIdentifier:@"ShowEventDetail" sender:event];
}

- (void)showBlockDetailViewWithBlock:(DCDBlock *)block
{
    [self performSegueWithIdentifier:@"ShowBlockDetail" sender:block];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"ShowEventDetail"]) {
        ShowEventDetailViewController *detailVC = segue.destinationViewController;
        DCDEvent *event = (DCDEvent *)sender;
        NSArray *speakers = [self.platformContext getSpeakersArrayFromEvent:event];
        detailVC.titleString = event->name_;
        detailVC.descriptionString = event->description__;
        detailVC.event = event;
        detailVC.speakers = speakers;
        detailVC.trackNumString = [NSString stringWithFormat:@"%ld", (self.track+1)];
        detailVC.dateTime = [self.platformContext getEventTimeFromStart:[event getStartFormatted] andEnd:[event getEndFormatted]];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)loadConferenceSchedule
{
    [self createSDASimple];
}

@end