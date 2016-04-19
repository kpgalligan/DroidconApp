//
//  PlatformContext_iOS.m
//  ios
//
//  Created by Sahil Ishar on 3/10/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

#import "PlatformContext_iOS.h"
#import "co/touchlab/droidconandroid/presenter/ConferenceDayHolder.h"
#import "co/touchlab/droidconandroid/presenter/ConferenceHourHolder.h"
#import "co/touchlab/droidconandroid/data/UserAccount.h"
#import "co/touchlab/droidconandroid/presenter/AppManager.h"
#import "java/util/ArrayList.h"

@implementation PlatformContext_iOS


-(id)init {
    if ( self = [super init] ) {
        self.iosContext = [DCPAppManager getContext];
    }
    return self;
}

- (AndroidContentContext *)appContext {
    return self.iosContext;
}

- (NSString *)storageDir
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    
    return documentsDirectory;
}

- (NSMutableArray *)convertIOSObjectArrayToArray:(IOSObjectArray *)objArray
{
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < objArray.length; i++) {
        DCPConferenceHourHolder *holder = [objArray objectAtIndex:i];
        [array addObject:holder];
    }

    return array;
}

- (NSArray *)getHourBlocksArray
{
    IOSObjectArray *objArray;
    NSMutableArray *array = [[NSMutableArray alloc] init];
    NSArray *sortedArray;
    
    if (!self.isDayTwo) {
        DCPConferenceDayHolder *daySchedule = [self.conferenceDays objectAtIndex:0];
        objArray = daySchedule->hourHolders_;
    } else {
        DCPConferenceDayHolder *daySchedule = [self.conferenceDays objectAtIndex:1];
        objArray = daySchedule->hourHolders_;
    }
    [array addObjectsFromArray:[self convertIOSObjectArrayToArray:objArray]];
    
    if (self.dateFormatter == nil) {
        self.dateFormatter = [[NSDateFormatter alloc] init];
        [self.dateFormatter setDateFormat:@"hh:mma"];
    }
    
    sortedArray = [array sortedArrayUsingComparator:^NSComparisonResult(id a, id b) {
        DCPConferenceHourHolder *holderA = (DCPConferenceHourHolder *)a;
        DCPConferenceHourHolder *holderB = (DCPConferenceHourHolder *)b;
        NSString *first = holderA->hourString_;
        NSString *second = holderB->hourString_;
        NSDate *date1 = [self.dateFormatter dateFromString:first];
        NSDate *date2 = [self.dateFormatter dateFromString:second];
        return [date1 compare:date2];
    }];
    
    return sortedArray;
}

- (NSArray *)getSpeakersArrayFromEvent:(DCDEvent *)event
{
    JavaUtilArrayList *speakersList = (JavaUtilArrayList *)event->speakerList_;
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < speakersList.size; i++) {
        [array addObject:[speakersList getWithInt:(jint)i]];
    }
    
    return [array copy];
}

- (NSString *)formatSpeakerStringFromArray:(NSArray *)array
{
    NSMutableArray *speakerNames = [[NSMutableArray alloc] init];
    
    for (DCDUserAccount *speaker in array) {
        [speakerNames addObject:speaker->name_];
    }
    
    return [speakerNames componentsJoinedByString:@", "];
}

- (NSString *)getEventTimeFromStart:(NSString *)startTime andEnd:(NSString *)endTime
{
    int startLoc = 7;
    int endLoc = 7;
    int startLocCutoff = 0;
    
    NSString *startFirstDigit = [startTime substringWithRange:NSMakeRange(startTime.length-7, 1)];
    NSString *endFirstDigit = [endTime substringWithRange:NSMakeRange(endTime.length-7, 1)];
    
    if ([startFirstDigit isEqualToString:@"0"]) {
        startLoc = 6;
    }
    if ([endFirstDigit isEqualToString:@"0"]) {
        endLoc = 6;
    }
    
    NSString *startAmPm = [startTime substringWithRange:NSMakeRange(startTime.length-2, 2)];
    NSString *endAmPm = [endTime substringWithRange:NSMakeRange(endTime.length-2, 2)];
    if ([startAmPm isEqualToString:endAmPm]) {
        startLocCutoff = 2;
    }
    
    NSString *startTimeStr = [startTime substringWithRange:NSMakeRange(startTime.length-startLoc, startLoc-startLocCutoff)];
    NSString *endTimeStr = [endTime substringWithRange:NSMakeRange(endTime.length-endLoc, endLoc)];
    
    return [NSString stringWithFormat:@"%@ - %@", startTimeStr, endTimeStr];
}

- (NSString *)getFullEventTime:(DCDEvent *)event
{
    NSString *day = [[event getStartFormatted] substringWithRange:NSMakeRange(0, 10)];

    if (self.timeFormatter == nil) {
        self.timeFormatter = [[NSDateFormatter alloc] init];
    }
    
    [self.timeFormatter setDateFormat:@"MM/dd/yyyy"];
    NSDate* myDate = [self.timeFormatter dateFromString:day];
    [self.timeFormatter setDateFormat:@"MMMM dd"];
    NSString *dayString = [self.timeFormatter stringFromDate:myDate];
    
    return [NSString stringWithFormat:@"%@, %@", dayString, [self getEventTimeFromStart:[event getStartFormatted] andEnd:[event getEndFormatted]]];
}

- (void)updateTableData
{
    [self.hourBlocks removeAllObjects];
    [self.hourBlocks addObjectsFromArray:[self getHourBlocksArray]];
}

- (void)loadCallbackWithDCPConferenceDayHolderArray:(IOSObjectArray *)conferenceDayHolders
{
    if (!self.successfulRequest) {
        self.hourBlocks = [[NSMutableArray alloc] init];
        
        self.conferenceDays = (NSArray *)conferenceDayHolders;
        [self updateTableData];
        [self.reloadDelegate reloadTableView];
        
        self.successfulRequest = YES;
        NSLog(@"Received data from server.");
    }
}

#pragma Table View - Delegate
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView{
    if(self.hourBlocks == nil)
        return 0;
    else
        return [self.hourBlocks count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    DCPConferenceHourHolder *hourHolder = [self.hourBlocks objectAtIndex:section];
    NSArray *scheduleBlocks = (NSArray *)hourHolder->scheduleBlocks_;
    return [scheduleBlocks count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *cellIdentifier = @"cellIdentifier";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
    }
    
    DCPConferenceHourHolder *hourHolder = [self.hourBlocks objectAtIndex:[indexPath section]];
    NSArray *scheduleBlocks = (NSArray *)hourHolder->scheduleBlocks_;
    
    NSObject *eventObj = [scheduleBlocks objectAtIndex:[indexPath row]];
    if ([eventObj isKindOfClass:[DCDEvent class]]) {
        DCDEvent *event = (DCDEvent *)eventObj;
        [cell.textLabel setText:event->name_];
        NSArray *speakers = [self getSpeakersArrayFromEvent:event];
        [cell.detailTextLabel setText:[self formatSpeakerStringFromArray:speakers]];
        
        [cell.textLabel setTextColor:[UIColor blackColor]];
        [cell.detailTextLabel setTextColor:[UIColor colorWithRed:(151/255.0) green:(151/255.0) blue:(151/255.0) alpha:1.0]];
        [cell setBackgroundColor:[UIColor whiteColor]];
        [cell setUserInteractionEnabled:YES];
    } else {
        DCDBlock *event = (DCDBlock *)eventObj;
        [cell.textLabel setText:event->name_];
        [cell.detailTextLabel setText:[self getEventTimeFromStart:[event getStartFormatted] andEnd:[event getEndFormatted]]];
        
        [cell.textLabel setTextColor:[UIColor colorWithRed:(87/255.0) green:(125/255.0) blue:(140/255.0) alpha:1.0]];
        [cell.detailTextLabel setTextColor:[UIColor colorWithRed:(87/255.0) green:(125/255.0) blue:(140/255.0) alpha:1.0]];
        [cell setBackgroundColor:[UIColor lightGrayColor]];
        [cell setUserInteractionEnabled:NO];
    }
    
    cell.layer.opaque = YES;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    DCPConferenceHourHolder *hourHolder = [self.hourBlocks objectAtIndex:[indexPath section]];
    NSArray *scheduleBlocks = (NSArray *)hourHolder->scheduleBlocks_;
    
    NSObject *eventObj = [scheduleBlocks objectAtIndex:[indexPath row]];
    if ([eventObj isKindOfClass:[DCDEvent class]]) {
        DCDEvent *event = (DCDEvent *)eventObj;
        [self.reloadDelegate showEventDetailViewWithEvent:event andIndex:[indexPath row]];
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, tableView.frame.size.width, 30)];
    /* Create custom view to display section header... */
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(20, -3, tableView.frame.size.width-40, 20)];
    [label setFont:[UIFont systemFontOfSize:12.0]];
    [label setTextColor:[UIColor colorWithRed:(87/255.0) green:(125/255.0) blue:(140/255.0) alpha:1.0]];
    [label setTextAlignment:NSTextAlignmentCenter];
    DCPConferenceHourHolder *hourHolder = [self.hourBlocks objectAtIndex:section];
    NSString *sectionName = hourHolder->hourString_;
    [label setText:sectionName];
    [headerView addSubview:label];
    [headerView setBackgroundColor:[UIColor clearColor]];
    
    return headerView;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 30.0;
}


@end
