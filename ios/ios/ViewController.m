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
#import <Crashlytics/Crashlytics.h>


@interface ViewController ()

@property (nonatomic, assign) long track;
@property (nonatomic, strong) NSMutableArray *notesArray;
@property (nonatomic, strong) NSMutableArray *imagesArray;
@property (nonatomic, strong) NSMutableArray *conferenceDays;
@property (nonatomic, strong) PlatformContext_iOS *platformContext;
@property (nonatomic, strong) DCPConferenceDataPresenter *dataPresenter;
@property (nonatomic, strong) JavaUtilArrayList *notes;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.tableHeaderView = nil;
    self.tableView.tableFooterView = nil;
    
    [self loadConferenceSchedule];
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
    self.dataPresenter = [[DCPConferenceDataPresenter alloc] initWithAndroidContentContext:[DCPAppManager getContext] withDCPConferenceDataHost:pcios withBoolean:true];
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

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.navigationController.navigationBar.barTintColor = [UIColor colorWithRed:(0/255.0) green:(90/255.0) blue:(224/255.0) alpha:1.0];
    self.navigationController.navigationBar.translucent = false;
    [self.navigationController.navigationBar setTitleTextAttributes : @{NSForegroundColorAttributeName: [UIColor whiteColor]}];
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