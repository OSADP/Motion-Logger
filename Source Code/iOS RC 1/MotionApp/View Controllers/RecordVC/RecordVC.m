//
//  ViewController.m

#import "RecordVC.h"
#import "RecordCell.h"
#import "GpsObject.h"
#import "CompassObject.h"
#import "AccelObject.h"
#import "GyroScopeObject.h"
#import "RecordVC.h"
#import "DataBaseManager.h"
#import "ReviewVC.h"
#import "HttpWorker.h"
#import "MResources.h"
#import "ContextObject.h"
#import "CloudObject.h"
#import "Checkin.h"

#define kBgQueue dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)


static int record_Id;

@interface RecordVC ()<UIActionSheetDelegate>
{
    NSDateFormatter * formatter;
    NSMutableArray * keysArray;
    NSMutableDictionary * m_dic;
    NSMutableArray * gpsArray;
    NSMutableArray * accelArray;
    NSMutableArray * gyroScope;
    NSMutableArray * compassArray;
    NSMutableArray * contextArray;
    NSMutableArray * cloudArray;
    NSString * startTime;
    NSString * currentTime;
    int counter;
    NSTimer * timer;
    NSTimer * timerForCloudThink;
    int flagForStartStopBtn;
    int flafForPauseResumeBtn;
    int isGyroOn;
    int isComOn;
    int isGpsOn;
    int isAccOn;
    NSUserDefaults * userDefaults;
    NSString * gpsQuality;
    NSString * motionContext;
    NSString * cloudThinkImage;
    NSString * cloudThinkConnectedStatus;
    UIBackgroundTaskIdentifier counterTask;
    NSString* strCheckIn;
    
    int m_nTimeForTCP;
    NSDate* m_dateForTCP;
    int m_nTimeForCheckin;
    BOOL m_bShowedCheckinView;
    BOOL checkinVChasPopped;
}

@end

@implementation RecordVC

@synthesize locationManger;
@synthesize motionManager;
//@synthesize recordsArray;
@synthesize motionActivity;


- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    cloudThinkConnectedStatus = @"";
    [userDefaults setFloat:1.0 forKey:kUSER_DEFAULT_ACCVALUE];
    [userDefaults setFloat:1.0 forKey:kUSER_DEFAULT_GYROVALUE];
    gpsQuality = @"";
    motionContext = @"";
    cloudThinkImage = @"circle_red.png";
    record_Id = 0;
    userDefaults = [NSUserDefaults standardUserDefaults];
    isGyroOn = 1;
    isComOn = 1;
    isGpsOn = 1;
    isAccOn =1;
    [userDefaults setInteger:isAccOn forKey:kUSER_DEFAULT_ISACCON];
    [userDefaults setInteger:isGyroOn forKey:kUSER_DEFAULT_ISGYROON];
    [userDefaults setInteger:isComOn forKey:kUSER_DEFAULT_ISCOMON];
    [userDefaults setInteger:isGpsOn forKey:kUSER_DEFAULT_ISGPSON];
    
    recordsArray = [[NSMutableArray alloc] init];
    flafForPauseResumeBtn = 0;
    pauseBtn.enabled = NO;
    counter = 0;
    flagForStartStopBtn = 0;
    formatter = [[NSDateFormatter alloc]init];
    [formatter setDateFormat: @"yyyy-MM-dd HH:mm:ss:SSSS"];
    
    keysArray = [[NSMutableArray alloc]initWithObjects:@"Run", @"Last Readings", @"Check-in", nil];
    NSMutableArray * runsArray = [[NSMutableArray alloc]initWithObjects:@"Start Time", @"Duration", nil];
    NSMutableArray * lastReadingsArray = [[NSMutableArray alloc]initWithObjects:@"GPS Qual.", @"Position", @"Accel.", @"Heading", @"Rotation", @"Context", @"CloudThink", nil];
    NSMutableArray * checkinArray = [[NSMutableArray alloc]initWithObjects:@"Check-in", nil];
    
    m_dic = [[NSMutableDictionary alloc]init];
    [m_dic setObject:runsArray forKey:@"Run"];
    [m_dic setObject:lastReadingsArray forKey:@"Last Readings"];
    [m_dic setObject:checkinArray forKey:@"Check-in"];
    
    gpsArray = [[NSMutableArray alloc]init];
    accelArray = [[NSMutableArray alloc]init];
    gyroScope = [[NSMutableArray alloc]init];
    compassArray = [[NSMutableArray alloc]init];
    contextArray = [[NSMutableArray alloc]init];
    cloudArray = [[NSMutableArray alloc]init];

    self.locationManger = [[CLLocationManager alloc]init];
    self.locationManger.delegate = self;
    if ([self.locationManger respondsToSelector:@selector(requestAlwaysAuthorization)]) {
        [self.locationManger requestAlwaysAuthorization];
    }
    [self.locationManger startMonitoringSignificantLocationChanges];
    [self.locationManger startUpdatingLocation];

    self.motionManager = [[CMMotionManager alloc] init];
    self.motionActivity = [[CMMotionActivityManager alloc]init];
    self.motionManager.accelerometerUpdateInterval = [[userDefaults objectForKey:kUSER_DEFAULT_ACCVALUE] floatValue];
    self.motionManager.gyroUpdateInterval = [[userDefaults objectForKey:kUSER_DEFAULT_GYROVALUE] floatValue];

    dispatch_queue_t mainQueue = dispatch_get_main_queue();
	asyncSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:mainQueue];
    
    m_nTimeForTCP = 0;
    m_dateForTCP = [NSDate date];
    m_nTimeForCheckin = 0;
    m_bShowedCheckinView = FALSE;
    checkinVChasPopped = FALSE;
}

- (void)viewWillAppear:(BOOL)animated
{
    isGpsOn = (int)[userDefaults integerForKey:kUSER_DEFAULT_ISGPSON];
    isComOn = (int)[userDefaults integerForKey:kUSER_DEFAULT_ISCOMON];
    isGyroOn = (int)[userDefaults integerForKey:kUSER_DEFAULT_ISGYROON];
    isAccOn = (int)[userDefaults integerForKey:kUSER_DEFAULT_ISACCON];
    
    self.motionManager.accelerometerUpdateInterval = 1 / [[userDefaults objectForKey:kUSER_DEFAULT_ACCVALUE] floatValue];
    self.motionManager.gyroUpdateInterval = 1 / [[userDefaults objectForKey:kUSER_DEFAULT_GYROVALUE] floatValue];
    m_bShowedCheckinView = FALSE;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)sendTCP:(id)sender {
    NSString* strTCPAddress = [userDefaults objectForKey:kUSER_DEFAULT_TCPSEVERADDRESS];
    NSString* strTCPPort = [userDefaults objectForKey:kUSER_DEFAULT_TCPPORT];
    if (!strTCPAddress || !strTCPPort)
    {
        return;
    }

    uint16_t port = [strTCPPort intValue];
    NSError *error = nil;
    if ([asyncSocket connectToHost:strTCPAddress onPort:port error:&error])
    {
    }
    else
    {
        NSLog(@"%@", [error description]);
    }
}

//Start Button Action
- (IBAction)startBtn:(id)sender
{
    if (flagForStartStopBtn == 0)
    {
        [startBtn setTitle:@"Stop" forState:UIControlStateNormal];
        
        m_gIndexCheckin=-1;
        m_nTimeForTCP = 0;
        m_nTimeForCheckin = 0;
        m_bShowedCheckinView = FALSE;
        checkinVChasPopped = FALSE;
        
        strCheckIn = @"Unknown";
        pauseBtn.enabled = YES;
        flagForStartStopBtn = 1;
        NSDate * date = [NSDate date];
        startTime = [formatter stringFromDate:date];

        if (isComOn == 1)
        {
            [self.locationManger startUpdatingHeading];
        }
        
        if (isGpsOn == 1)
        {
            [self.locationManger startUpdatingLocation];
        }
        
        if (isAccOn == 1)
        {
            [self.motionManager startAccelerometerUpdatesToQueue:[NSOperationQueue currentQueue]
                                                     withHandler:^(CMAccelerometerData  *accelerometerData, NSError *error) {
                                                         [self outputAccelertionData:accelerometerData.acceleration];
                                                         if(error){
                                                             
                                                             NSLog(@"%@", error);
                                                         }
                                                     }];
        }
        
        if (isGyroOn == 1)
        {
            [self.motionManager startGyroUpdatesToQueue:[NSOperationQueue currentQueue]
                                            withHandler:^(CMGyroData *gyroData, NSError *error) {
                                                [self outputRotationData:gyroData.rotationRate];
                                            }];
        }
        
        //Motion State
        if ([CMMotionActivityManager isActivityAvailable])
        {
            [self.motionActivity startActivityUpdatesToQueue:[[NSOperationQueue alloc] init]
                                                 withHandler:
             ^(CMMotionActivity *activity) {
                 
                 [self outputActivityData:activity];
             }];
            
        }
        else
        {
            motionContext = @"Not Available in Device!";
        }
        
        //CloudThink
        
        timerForCloudThink = [NSTimer scheduledTimerWithTimeInterval:30.0 target:self selector:@selector(callCloudThinkService) userInfo:Nil repeats:YES];
        
        timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(setTimerValue:) userInfo:Nil repeats:YES];
        
        NSRunLoop *runner = [NSRunLoop currentRunLoop];
        [runner addTimer:timer forMode: NSDefaultRunLoopMode];
        m_nTimeForTCP = 0;
        m_nTimeForCheckin = 0;
        m_bShowedCheckinView = FALSE;
    }
    else
    {
        UIActionSheet * actionSheet = [[UIActionSheet alloc]initWithTitle:@"Save Log" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:@"Discard Log" otherButtonTitles:@"Save Log", nil];
        actionSheet.actionSheetStyle = UIActionSheetStyleBlackOpaque;
        [actionSheet showInView:[self.view window]];
//        [actionSheet showInView:[UIApplication sharedApplication].keyWindow];
//        [actionSheet showFromTabBar:self.tabBarController.tabBar];
        
        [startBtn setTitle:@"Start" forState:UIControlStateNormal];
        [pauseBtn setTitle:@"Pause" forState:UIControlStateNormal];
        flafForPauseResumeBtn = 0;
        pauseBtn.enabled = NO;
        flagForStartStopBtn = 0;
        [timerForCloudThink invalidate];
        [timer invalidate];
        [self.locationManger stopUpdatingHeading];
        [self.locationManger stopUpdatingLocation];
        [self.motionManager stopGyroUpdates];
        [self.motionManager stopAccelerometerUpdates];
        if ([CMMotionActivityManager isActivityAvailable])
        {
            [self.motionActivity stopActivityUpdates];
            motionContext = @"";
        }
        else
        {
            motionContext = @"";
        }
        [[UIApplication sharedApplication] endBackgroundTask:counterTask];
    }
}


- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;
{
    if (buttonIndex == actionSheet.destructiveButtonIndex)
    {
        //destructive and new start
        [gpsArray removeAllObjects];
        [accelArray removeAllObjects];
        [gyroScope removeAllObjects];
        [compassArray removeAllObjects];
        [contextArray removeAllObjects];
        [cloudArray removeAllObjects];
        startTime = @"";
        currentTime = @"";
        flafForPauseResumeBtn = 0;
        pauseBtn.enabled = NO;
        counter = 0;
        flagForStartStopBtn = 0;
        [recordTableView reloadData];
        m_bShowedCheckinView = FALSE;
    }
    else
    {
        //Save
        RecordObject * recordObject = [[RecordObject alloc]init];
        recordObject.record_name = @"Unnamed Record";
        recordObject.record_time = [formatter dateFromString:startTime];
        recordObject.record_duration = counter;
        recordObject.gpsObject = [NSMutableArray arrayWithArray:gpsArray];
        recordObject.gyroScopeObject = [NSMutableArray arrayWithArray:gyroScope];
        recordObject.accelObject = [NSMutableArray arrayWithArray:accelArray];
        recordObject.compassObject = [NSMutableArray arrayWithArray:compassArray];
        recordObject.contextArray = [NSMutableArray arrayWithArray:contextArray];
        recordObject.cloudArray = [NSMutableArray arrayWithArray:cloudArray];
        recordObject.isAccOn = isAccOn;
        recordObject.isComOn = isComOn;
        recordObject.isGyroOn = isGyroOn;
        recordObject.isGpsOn = isGpsOn;
        [recordsArray addObject:recordObject];
        
//        UINavigationController * recordNav = (UINavigationController *) [self.tabBarController.viewControllers objectAtIndex:1];
//        ReviewVC * reviewVC = (ReviewVC *)[recordNav.viewControllers objectAtIndex:0];
//        reviewVC.recordsArray = recordsArray;
        //        NSUserDefaults * userDefaults = [NSUserDefaults standardUserDefaults];
        //        [userDefaults setObject:self.recordsArray forKey:@"recordsArray"];
        //        [userDefaults synchronize];
        
        [gpsArray removeAllObjects];
        [accelArray removeAllObjects];
        [gyroScope removeAllObjects];
        [compassArray removeAllObjects];
        [contextArray removeAllObjects];
        [cloudArray removeAllObjects];
        startTime = @"";
        currentTime = @"";
        flafForPauseResumeBtn = 0;
        pauseBtn.enabled = NO;
        counter = 0;
        flagForStartStopBtn = 0;
        gpsQuality = @"";
        [recordTableView reloadData];
        // TODO: Save persistent here
    }
}

- (void)saveRecord
{
    dispatch_async(kBgQueue, ^{
        NSMutableArray * newRecordArray = [[[DataBaseManager alloc]init] selectRecords];
        RecordObject * lastRecord = [newRecordArray lastObject];
        if (lastRecord)
            [self performSelectorOnMainThread:@selector(saveRecordSensors:) withObject:[NSNumber numberWithInt:lastRecord.record_id] waitUntilDone:YES];
    });
}

- (void)saveRecordSensors:(NSNumber *)record_id
{
    DataBaseManager * dataBaseManager = [[DataBaseManager alloc]init];
    NSLog(@"id: %d",[record_id intValue]);
    
    dispatch_async(kBgQueue, ^{
        for (GpsObject * gps in gpsArray)
        {
            [dataBaseManager insertGps:gps WithRecordId:[record_id intValue]];
        }
        
        for (GyroScopeObject * gyro in gyroScope)
        {
            [dataBaseManager insertGyro:gyro WithRecordId:[record_id intValue]];
        }
        
        for (CompassObject * compass in compassArray)
        {
            [dataBaseManager insertCompass:compass WithRecordId:[record_id intValue]];
        }
        
        for (AccelObject * accel in accelArray)
        {
            [dataBaseManager insertAccel:accel WithRecordId:[record_id intValue]];
        }
        [self performSelectorOnMainThread:@selector(updateUIAfterSavingData) withObject:Nil waitUntilDone:YES];
        
    });
}


- (void)updateUIAfterSavingData
{
    [gpsArray removeAllObjects];
    [accelArray removeAllObjects];
    [gyroScope removeAllObjects];
    [compassArray removeAllObjects];
    [recordsArray removeAllObjects];
    [contextArray removeAllObjects];
    [cloudArray removeAllObjects];
    startTime = @"";
    currentTime = @"";
    flafForPauseResumeBtn = 0;
    pauseBtn.enabled = NO;
    counter = 0;
    flagForStartStopBtn = 0;
    [recordTableView reloadData];
}

- (IBAction)pauseBtn:(id)sender
{

    if (flafForPauseResumeBtn == 0)
    {
        [pauseBtn setTitle:@"Resume" forState:UIControlStateNormal];
        flafForPauseResumeBtn = 1;
        [timer invalidate];
    }
    else
    {
        [pauseBtn setTitle:@"Pause" forState:UIControlStateNormal];
        flafForPauseResumeBtn = 0;
        NSDate * date = [NSDate date];
        startTime = [formatter stringFromDate:date];
        
        if (isComOn == 1)
        {
            [self.locationManger startUpdatingHeading];
        }
        
        if (isGpsOn == 1)
        {
            [self.locationManger startUpdatingLocation];
        }
        
        if (isAccOn == 1)
        {
            [self.motionManager startAccelerometerUpdatesToQueue:[NSOperationQueue currentQueue]
                                                     withHandler:^(CMAccelerometerData  *accelerometerData, NSError *error) {
                                                         [self outputAccelertionData:accelerometerData.acceleration];
                                                         if(error){
                                                             
                                                             NSLog(@"%@", error);
                                                         }
                                                     }];
        }
        
        if (isGyroOn == 1)
        {
            [self.motionManager startGyroUpdatesToQueue:[NSOperationQueue currentQueue]
                                            withHandler:^(CMGyroData *gyroData, NSError *error) {
                                                [self outputRotationData:gyroData.rotationRate];
                                            }];
        }
        
        [self runTimerInBackground];
       
    }
}

- (void)runTimerInBackground
{
    counterTask = [[UIApplication sharedApplication]beginBackgroundTaskWithExpirationHandler:^{
        //...
    }];
    timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(setTimerValue:) userInfo:Nil repeats:YES];
    
    NSRunLoop *runner = [NSRunLoop currentRunLoop];
    [runner addTimer:timer forMode: NSDefaultRunLoopMode];
}

- (void)setTimerValue:(NSTimer *)timer
{
    Checkin* vc = [[Checkin alloc] initWithNibName:@"Checkin" bundle:nil];
    
    counter = counter + 1;
    //NSLog(@"running: %d",counter);
    currentTime = [self timeFormatted:counter];
    [recordTableView reloadData];
    m_nTimeForTCP++;
    if (m_nTimeForTCP == 60) // Go every 60 seconds
    {
        if ([asyncSocket isConnected])
        {
            [self sendDataViaTCP];
        }
        else {
            [self sendTCP:nil];
        }
        
        m_nTimeForTCP = 0;
        m_dateForTCP = [NSDate date];
    }
    
    if (!m_bShowedCheckinView) // Show the first run
    {
        m_bShowedCheckinView = TRUE;
        // Checkin
        if (checkinVChasPopped == FALSE) {
            checkinVChasPopped = TRUE;
            [self.navigationController pushViewController:vc animated:YES];
        }
    }

    m_nTimeForCheckin++;
    if (m_nTimeForCheckin == 300) // Go every 5 minutes (300 seconds)
    {
        NSLog(@"Checking in: %d",m_nTimeForCheckin);
        m_nTimeForCheckin = 0;
        // Checkin
        //if (checkinVChasPopped == FALSE) { // Ignore the check here, because the computer will only pop it once regardless
        // The only issue here is that if the user ignores the prompts for too long, a queue of screens can and will pile up
        // To be fixed... later... by someone else, hopefully
            checkinVChasPopped = TRUE;
            [self.navigationController pushViewController:vc animated:YES];
        //}
    }
    
    
}

- (void)locationManager:(CLLocationManager *)manager
       didUpdateHeading:(CLHeading *)newHeading
{
    NSDate * date = [NSDate date];
    
    //Heading
    CompassObject * compassObject = [[CompassObject alloc]init];
    compassObject.timeStamp = [formatter stringFromDate:date];
    compassObject.magHeading = [NSString stringWithFormat:@"%.4f", newHeading.magneticHeading];
    compassObject.trueHeading = [NSString stringWithFormat:@"%.4f", newHeading.trueHeading];
    NSDictionary * dic = [NSDictionary dictionaryWithObject:compassObject forKey:[formatter stringFromDate:[NSDate date]]];
    [compassArray addObject:dic];
}

- (void)locationManager:(CLLocationManager *)manager
	 didUpdateLocations:(NSArray *)locations
{
    CLLocation * lastLocation = [locations lastObject];
    
    //GPS Desired Accuracy "GPS QUality"
    gpsQuality = [NSString stringWithFormat:@"%f",lastLocation.horizontalAccuracy];
    //NSLog(@"gpsQuality: %@",gpsQuality);
    
    //Postion
    GpsObject * gpsObject = [[GpsObject alloc]init];
    NSDate * date = [NSDate date];
    gpsObject.timeStamp = [formatter stringFromDate:date];
    gpsObject.log = [NSString stringWithFormat:@"%.6f", lastLocation.coordinate.longitude];
    gpsObject.lat = [NSString stringWithFormat:@"%.6f", lastLocation.coordinate.latitude];
    gpsObject.height = [NSString stringWithFormat:@"%.6f",lastLocation.altitude];
    NSDictionary * dic = [NSDictionary dictionaryWithObject:gpsObject forKey:[formatter stringFromDate:[NSDate date]]];
    [gpsArray addObject:dic];
    
}

-(void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    NSLog(@"%@", error.localizedDescription);
}

-(void)outputAccelertionData:(CMAcceleration)acceleration
{
    //Accel.
    AccelObject * accelObject = [[AccelObject alloc]init];
    NSDate * date = [NSDate date];
    accelObject.timeStamp = [formatter stringFromDate:date];
    accelObject.x = [NSString stringWithFormat:@" %.6fg",acceleration.x];
    accelObject.y = [NSString stringWithFormat:@" %.6fg",acceleration.y];
    accelObject.z = [NSString stringWithFormat:@" %.6fg",acceleration.z];
    //NSLog(@"accel_x: %@",accelObject.x);
    NSDictionary * dic = [NSDictionary dictionaryWithObject:accelObject forKey:[formatter stringFromDate:[NSDate date]]];
    [accelArray addObject:dic];
}


-(void)outputRotationData:(CMRotationRate)rotation
{
    //Rotation
    GyroScopeObject * gyroScopeObject = [[GyroScopeObject alloc]init];
    NSDate * date = [NSDate date];
    gyroScopeObject.timeStamp = [formatter stringFromDate:date];
    gyroScopeObject.x = [NSString stringWithFormat:@" %.6f",rotation.x];
    gyroScopeObject.y = [NSString stringWithFormat:@" %.6f",rotation.y];
    gyroScopeObject.z = [NSString stringWithFormat:@" %.6f",rotation.z];
    NSDictionary * dic = [NSDictionary dictionaryWithObject:gyroScopeObject forKey:[formatter stringFromDate:[NSDate date]]];
    [gyroScope addObject:dic];
}

- (void)outputActivityData:(CMMotionActivity *)activity
{
    //NSString* strContext = motionContext;
    if ([activity walking])
    {
        motionContext = @"Walking";
    }
    else if ([activity running])
    {
        motionContext = @"Running";
    }
    else if ([activity automotive])
    {
        motionContext = @"Automotive";
    }
    else if ([activity stationary])
    {
        motionContext = @"Stationary";
    }
    else if ([activity unknown])
    {
        motionContext = @"Unknown";
    }
    /*
    if (![strContext isEqualToString:motionContext])
    {
        if (!m_bShowedCheckinView)
        {
            m_bShowedCheckinView = TRUE;
            // Checkin
            Checkin* vc = [[Checkin alloc] initWithNibName:@"Checkin" bundle:nil];
            [self.navigationController pushViewController:vc animated:NO];
        }
    }
    */
    
    ContextObject * contextObject = [[ContextObject alloc]init];
    NSDate * currentDate = [NSDate date];
    contextObject.timeStamp = [formatter stringFromDate:currentDate];
    contextObject.contextValue = motionContext;
    NSDictionary * dic = [NSDictionary dictionaryWithObject:contextObject forKey:[formatter stringFromDate:[NSDate date]]];
    [contextArray addObject:dic];
    //NSLog(@"Adding to context object: %@",dic);
    //NSLog(@"motionContext: %@",motionContext);
}

- (void)callCloudThinkService
{
    NSString * vin = [userDefaults valueForKey:kUSER_DEFAULT_VINVALUE];
    //vin=[@"VIN" stringByAppendingString:vin];
    NSString * api_url = [NSString stringWithFormat:@"https://api.cloud-think.com/data/%@",vin];
    HttpWorker * httpWorker = [[HttpWorker alloc]init];
    [httpWorker requestNetwork:api_url];
    [httpWorker setDelegate:[[MResources getResources] getCloudThinkParserClass]];
    [[[MResources getResources] getCloudThinkParserClass]setDelegate:self];
}

- (void)updateView:(BOOL)isConnected
{
    if (isConnected)
    {
        cloudThinkImage = @"circle.png";
        cloudThinkConnectedStatus = @"Connected Recently";
    }
    else
    {
        cloudThinkImage = @"circle_red.png";
        cloudThinkConnectedStatus = @"Not Connected";
    }
    strCheckIn = @"Unknown";
    if (m_gIndexCheckin != -1)
    {
        strCheckIn = [m_gAryCheckin objectAtIndex:m_gIndexCheckin];
    }
    
    CloudObject * cloudObject = [[CloudObject alloc]init];
    NSDate * currentDate = [NSDate date];
    cloudObject.timeStamp = [formatter stringFromDate:currentDate];
    cloudObject.cloudValue = cloudThinkConnectedStatus;
    cloudObject.cloudThinkImage = cloudThinkImage;
    cloudObject.Checkin = strCheckIn;
    NSDictionary * dic = [NSDictionary dictionaryWithObject:cloudObject forKey:[formatter stringFromDate:[NSDate date]]];
    [cloudArray addObject:dic];
    //NSLog(@"Adding to Cloud object: %@",dic);
//    [recordTableView reloadData];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString * header = [keysArray objectAtIndex:section];
    NSMutableArray * temp = [m_dic objectForKey:header];
    return temp.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    RecordCell * cell = (RecordCell *) [tableView dequeueReusableCellWithIdentifier:@"RecordCell"];
    if (!cell)
    {
        NSArray *topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"RecordCell" owner:self options:nil];
        cell = [topLevelObjects objectAtIndex:0];
    }
    
    NSString * header = [keysArray objectAtIndex:indexPath.section];
    NSMutableArray * temp = [m_dic objectForKey:header];
    NSString * text = [temp objectAtIndex:indexPath.row];
    
    switch (indexPath.section) {
        case 0:
            if (indexPath.row == temp.count-1)
            {
                //Timer Value
                [cell CellText:text CellTextForValue:currentTime ? currentTime : @""];
            }
            else if (indexPath.row == temp.count-2)
            {
                //Start Time
                [cell CellText:text CellTextForValue:startTime ? startTime : @""];
            }
            break;
        case 1:
            if (indexPath.row == temp.count-1)
            {
                //CloudThink
                
                [cell CellText:text CellImage:[UIImage imageNamed:cloudThinkImage]];
            }
            else if(indexPath.row == temp.count-2)
            {
                //Context
                ContextObject * contextObject = [[contextArray lastObject] allObjects] && [[[contextArray lastObject] allObjects] count] > 0 ? [[[contextArray lastObject] allObjects] objectAtIndex:0] : nil;
                if (!contextObject.contextValue)
                {
                    [cell CellText:text CellTextForValue:@"Initializing"];
                }
                else
                {
                    [cell CellText:text CellTextForValue:contextObject.contextValue];
                }
            }
            else if (indexPath.row == temp.count-3)
            {
                //Rotation
                GyroScopeObject * gyroScopeObject = [[gyroScope lastObject] allObjects] && [[[gyroScope lastObject] allObjects] count] > 0 ? [[[gyroScope lastObject] allObjects] objectAtIndex:0] : nil;
                if (gyroScopeObject)
                {
                    [cell CellText:text CellTextForValue:[NSString stringWithFormat:@"%@, %@, %@",gyroScopeObject.x, gyroScopeObject.y,gyroScopeObject.z]];
                }
                else
                {
                    [cell CellText:text CellTextForValue:@"N/A"];
                }
            }
            else if (indexPath.row == temp.count-4)
            {
                //Heading
                CompassObject * compassObject = [[compassArray lastObject] allObjects] && [[[compassArray lastObject] allObjects] count] > 0 ? [[[compassArray lastObject] allObjects] objectAtIndex:0] : nil;
                if (compassObject)
                {
                    [cell CellText:text CellTextForValue:[NSString stringWithFormat:@"%@, %@",compassObject.magHeading, compassObject.trueHeading]];
                }
                else
                {
                    [cell CellText:text CellTextForValue:@"N/A"];
                }
            }
            else if (indexPath.row == temp.count-5)
            {
                //Accel.
                AccelObject * accelObject = [[accelArray lastObject] allObjects] && [[[accelArray lastObject] allObjects] count] > 0 ? [[[accelArray lastObject] allObjects] objectAtIndex:0] : nil;
                if (accelObject)
                {
                    [cell CellText:text CellTextForValue:[NSString stringWithFormat:@"%@, %@, %@",accelObject.x, accelObject.y,accelObject.z]];
                }
                else
                {
                    [cell CellText:text CellTextForValue:@"N/A"];
                }
                
            }
            else if (indexPath.row == temp.count-6)
            {
                //Postion
                GpsObject * gpsObject = [[gpsArray lastObject] allObjects] && [[[gpsArray lastObject] allObjects] count] > 0 ? [[[gpsArray lastObject] allObjects] objectAtIndex:0] : nil;
                if (gpsObject)
                {
                    [cell CellText:text CellTextForValue:[NSString stringWithFormat:@"%@, %@, %@",gpsObject.log,gpsObject.lat, gpsObject.height]];
                }
                else
                {
                    [cell CellText:text CellTextForValue:@""];
                }
                
            }
            else if (indexPath.row == temp.count-7)
            {
                //GPS Qual.
                [cell CellText:text CellTextForValue:gpsQuality];
            }
            break;
        case 2:
            if (indexPath.row == 0){
                [cell CellText:text CellTextForValue:@""];
            }
            break;
    }

    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    return cell;
}


- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return keysArray.count;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString * header = [keysArray objectAtIndex:section];
    return header;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    if (indexPath.section == 2 && indexPath.row == 0)
    {
        m_nTimeForCheckin = 0; // Reset checkin counter
        // Checkin
        Checkin* vc = [[Checkin alloc] initWithNibName:@"Checkin" bundle:nil];
        [self.navigationController pushViewController:vc animated:NO];
    }
}

- (NSString *)timeFormatted:(int)totalSeconds
{
    int secnd = totalSeconds % 60;
    int minte = (totalSeconds / 60) % 60;
    return [NSString stringWithFormat:@"%02d:%02d", minte, secnd];
}


/*-(NSString *)getUniqueDeviceIdentifierAsString
{
    
    NSString *appName=[[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleNameKey];
    
    NSString *strApplicationUUID = [SSKeychain passwordForService:appName account:@"incoding"];
    if (strApplicationUUID == nil)
    {
        strApplicationUUID  = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
        [SSKeychain setPassword:strApplicationUUID forService:appName account:@"incoding"];
    }
    
    return strApplicationUUID;
}
*/

- (void)sendDataViaTCP
{
    NSMutableString* strFileContent = [NSMutableString stringWithString:@"START\r\n"];
    // Add IMEI printing here
    // We can't read IMEI, so let's use this instead:
    //  @property(nonatomic, readonly, retain) NSUUID *identifierForVendor <-- this changes after an uninstall / reinstall. We can use the above getUniqueDeviceIdentifierString code to provide a constant ID that is stored in the keychain
    NSString *strApplicationUUID  = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    [strFileContent appendFormat:@"%@,%@\r\n",@"IMEI",strApplicationUUID]; // Not actually the IMEI
    [strFileContent appendFormat:@"%@,%@\r\n",@"USER", [userDefaults objectForKey:kUSER_DEFAULT_TCPUSERID] ? [userDefaults objectForKey:kUSER_DEFAULT_TCPUSERID] : @""];
    
    NSDate *DateBefore5Second = [[NSDate date] dateByAddingTimeInterval:-5]; // Go back 5 seconds...
    NSDate *DateBefore30Second = [[NSDate date] dateByAddingTimeInterval:-31]; // Go back 31 seconds...
    NSDate *DateBefore70Second = [[NSDate date] dateByAddingTimeInterval:-71]; // Go back 70 seconds...
    
    NSPredicate* predicate = [NSPredicate predicateWithBlock:^BOOL(id evaluatedObject, NSDictionary *bindings) {
        if (!evaluatedObject && [evaluatedObject allKeys].count == 0)
            return FALSE;
        NSDate* date = [formatter dateFromString:[[evaluatedObject allKeys] objectAtIndex:0]];
        if (date > DateBefore5Second) {
          return [date compare:DateBefore5Second] == NSOrderedDescending;
        }
        else {
            return FALSE;
        }
    }];

    NSPredicate* predicatelong = [NSPredicate predicateWithBlock:^BOOL(id evaluatedObject, NSDictionary *bindings) {
        if (!evaluatedObject && [evaluatedObject allKeys].count == 0)
            return FALSE;
        NSDate* date = [formatter dateFromString:[[evaluatedObject allKeys] objectAtIndex:0]];
        if (date > DateBefore30Second) {
            return [date compare:DateBefore5Second] == NSOrderedDescending;
        }
        else {
            return FALSE;
        }
    }];
 
    NSPredicate* predicateverylong = [NSPredicate predicateWithBlock:^BOOL(id evaluatedObject, NSDictionary *bindings) {
        if (!evaluatedObject && [evaluatedObject allKeys].count == 0)
            return FALSE;
        NSDate* date = [formatter dateFromString:[[evaluatedObject allKeys] objectAtIndex:0]];
        if (date > DateBefore70Second) {
            return [date compare:DateBefore5Second] == NSOrderedDescending;
        }
        else {
            return FALSE;
        }
    }];
    
    NSArray* aryFilterGps = [gpsArray filteredArrayUsingPredicate:predicate];
    NSArray* aryFilterAccel = [accelArray filteredArrayUsingPredicate:predicate];
    //NSArray* aryFilterCompass = [compassArray filteredArrayUsingPredicate:predicate];
    //NSArray* aryFilterGyroScope = [gyroScope filteredArrayUsingPredicate:predicate];
    NSArray* aryFilterContext = [contextArray filteredArrayUsingPredicate:predicateverylong];
    NSArray* aryFilterCloud = [cloudArray filteredArrayUsingPredicate:predicateverylong];
    
   //[strFileContent appendFormat:@"%@",@"GPS"];
    
    /* Send only one value */
    @try {
        GpsObject * gpsObject = [aryFilterGps[1] allValues] && [[aryFilterGps[1] allValues] count] > 0 ? [[aryFilterGps[1] allValues] objectAtIndex:0] : nil;
        if (gpsObject)
        {
            // GPS,<latitude>,<longitude>,<height>,<timestamp> where <latitude> and <longitude> are decimal, -123.123123123 or 12.12398543, height is 12312434.12312312312 in meters
            [strFileContent appendFormat:@"GPS,%@,%@,%@,%@\r\n",gpsObject.lat,gpsObject.log,gpsObject.height,gpsObject.timeStamp];
        }
    } @catch (NSException *exception) {}
    
    /* Temporarily disable printing of all values */
    /*
    for (NSDictionary* dic in aryFilterGps)
    {
        GpsObject * gpsObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (gpsObject)
        {
             // GPS,<latitude>,<longitude>,<height>,<timestamp> where <latitude> and <longitude> are decimal, -123.123123123 or 12.12398543, height is 12312434.12312312312 in meters
             [strFileContent appendFormat:@"GPS,%@,%@,%@,%@\r\n",gpsObject.lat,gpsObject.log,gpsObject.height,gpsObject.timeStamp];
        }
    }
    
    for (NSDictionary* dic in aryFilterAccel)
    {
        AccelObject * accelObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (accelObject)
        {
            float temp_accel_x = [accelObject.x doubleValue] * 9.801;
            float temp_accel_y = [accelObject.y doubleValue] * 9.801;
            float temp_accel_z = [accelObject.z doubleValue] * 9.801;
            [strFileContent appendFormat:@"ACCEL,%f,%f,%f,%@\r\n",temp_accel_x,temp_accel_y,temp_accel_z,accelObject.timeStamp];
        }
    }
    
    for (NSDictionary* dic in aryFilterCompass)
    {
        CompassObject * compassObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (compassObject)
        {
            [strFileContent appendFormat:@"COMPASS,%@,%@,%@\r\n",compassObject.magHeading,compassObject.trueHeading,compassObject.timeStamp];
        }
    }
    
    for (NSDictionary* dic in aryFilterGyroScope)
    {
        GyroScopeObject * gyroScopeObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (gyroScopeObject)
        {
            [strFileContent appendFormat:@"GYRO,%@,%@,%@,%@\r\n",gyroScopeObject.x,gyroScopeObject.y,gyroScopeObject.z,gyroScopeObject.timeStamp];
        }
    }

    for (NSDictionary* dic in aryFilterContext)
    {
        ContextObject * contextObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (contextObject)
        {
            [strFileContent appendFormat:@"CONTEXT,%@,%@\r\n",contextObject.contextValue,contextObject.timeStamp];
        }
    }

    for (NSDictionary* dic in aryFilterCloud)
    {
        CloudObject * cloudObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (cloudObject)
        {
            [strFileContent appendFormat:@"CLOUDTHINK,%@,%@\r\n",cloudObject.cloudValue, cloudObject.timeStamp];
        }
    }
    
    for (NSDictionary* dic in aryFilterCloud)
    {
        CloudObject * cloudObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (cloudObject)
        {
            [strFileContent appendFormat:@"GROUND,%@,%@\r\n", cloudObject.checkin, cloudObject.timeStamp];
        }
    }
    */
    
    

    // Add the MIT algorithm here
    NSMutableArray* AccelValues = [[NSMutableArray alloc] init];
    double tempAccelerationValue = 0;
    double magnitudeCalc = 0;
    double maxAcceleration = 0;
    double minAcceleration = 100;
    int objectCount = 0;
    
    for (NSDictionary* dic in aryFilterAccel)
    {
        AccelObject * accelObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (accelObject)
        {
            double xValue = [accelObject.x doubleValue];
            double yValue = [accelObject.y doubleValue];
            double zValue = [accelObject.z doubleValue];
            double accelMagnitude = sqrt(xValue * xValue + yValue * yValue + zValue * zValue)*9.80665;
            [AccelValues addObject:[NSNumber numberWithDouble:accelMagnitude]];
            objectCount++;
        }
    }

    //for (NSNumber *obj in AccelValues) {
    for (int i = 0; i < objectCount; i++) {
        tempAccelerationValue = [[AccelValues objectAtIndex:i] doubleValue];
        if (tempAccelerationValue > maxAcceleration) { maxAcceleration = tempAccelerationValue; } // Set the highest value to maximum
        if (tempAccelerationValue < minAcceleration) { minAcceleration = tempAccelerationValue; } // Set the lowest value to minimum
        magnitudeCalc = magnitudeCalc + tempAccelerationValue; // Take the sum of the means
    }
    magnitudeCalc = magnitudeCalc / objectCount; // Calculate the mean
    
    double line_between_bike_and_walk = 10.5;
    // TODO: add a new data storage type for MIT's assumed value so that this can be saved to the file and reopened later
    
    NSDate * dateToSend = [NSDate date];
    NSDateFormatter * formatterToSend = [[NSDateFormatter alloc]init];
    [formatterToSend setDateFormat: @"yyyy-MM-dd HH:mm:ss:SSSS"];
    NSString *dateStringToSend = [formatterToSend stringFromDate:dateToSend];
    
    [strFileContent appendFormat:@"ACCEL_STATS,%f,%f,%f,%@\r\n",minAcceleration,magnitudeCalc,maxAcceleration,dateStringToSend];
    
    [strFileContent appendFormat:@"MIT_ALGO,%@",@""];
    if (minAcceleration > 9) { [strFileContent appendFormat:@"%@",@"Stationary"]; }
    if ( (magnitudeCalc < line_between_bike_and_walk) && (minAcceleration <= 9) ) { [strFileContent appendFormat:@"%@,",@"Bicycling"]; }
    if ( magnitudeCalc >= 10.5 + 1.54 * minAcceleration) { [strFileContent appendFormat:@"%@,",@"Running"]; }
    else if ( magnitudeCalc >= line_between_bike_and_walk) { [strFileContent appendFormat:@"%@,",@"Walking"]; } // Only if running is not true
    [strFileContent appendFormat:@",%@\r\n",dateStringToSend];
   
    for (NSDictionary* dic in aryFilterContext)
    {
        ContextObject * contextObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (contextObject)
        {
            [strFileContent appendFormat:@"CONTEXT,%@,%@\r\n",contextObject.contextValue,contextObject.timeStamp];
        }
    }
    
    for (NSDictionary* dic in aryFilterCloud)
    {
        CloudObject * cloudObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (cloudObject)
        {
            [strFileContent appendFormat:@"CLOUDTHINK,%@,%@\r\n",cloudObject.cloudValue, cloudObject.timeStamp];
        }
    }
    
    for (NSDictionary* dic in cloudArray)
    {
        CloudObject * cloudObject = [dic allValues] && [[dic allValues] count] > 0 ? [[dic allValues] objectAtIndex:0] : nil;
        if (cloudObject)
        {
            [strFileContent appendFormat:@"GROUND,%@,%@\r\n",cloudObject.checkin, cloudObject.timeStamp];
        }
    }
    
    // Getting this to work and only show the newest results would be sweet, but...
    /*
     @try {
     [strFileContent appendFormat:@"\r\n%@\r\n ",@"CONTEXT"];
     ContextObject *contextObject = [contextArray lastObject];
     NSLog( @"Context Object: %@", contextObject.contextValue);
     [strFileContent appendFormat:@"%@, %@\r\n",contextObject.contextValue,contextObject.timeStamp];
     } @catch (NSException *exception) {
     
     // Print exception information
     NSLog( @"Context NSException caught" );
     NSLog( @"Name: %@", exception.name);
     NSLog( @"Reason: %@", exception.reason );
     }
     
     @try {
     [strFileContent appendFormat:@"\r\n%@\r\n ",@"CLOUDTHINK"];
     CloudObject *cloudObject = [cloudArray lastObject];
     NSLog( @"Cloud Object: %@", cloudObject);
     [strFileContent appendFormat:@"%@, %@\r\n",cloudObject.cloudValue, cloudObject.timeStamp];
     [strFileContent appendFormat:@"\r\n%@\r\n ",@"GROUND"];
     [strFileContent appendFormat:@"%@, %@\r\n",cloudObject.checkin, cloudObject.timeStamp];
     } @catch (NSException *exception) {
     // Print exception information
     NSLog( @"CloudThink NSException caught" );
     NSLog( @"Name: %@", exception.name);
     NSLog( @"Reason: %@", exception.reason );
     }
     */
    
    
    [strFileContent appendFormat:@"\r\n%@\r\n ",@"END"];
    
    NSLog(@"TCP send Data : %@",strFileContent);
    
    NSData* data = [strFileContent dataUsingEncoding:NSUTF8StringEncoding];
    [asyncSocket writeData:data withTimeout:-1 tag:0];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Socket Delegate
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)socket:(GCDAsyncSocket *)sock didConnectToHost:(NSString *)host port:(UInt16)port
{
    // Backgrounding doesn't seem to be supported on the simulator yet
    [sock performBlock:^{
        if ([sock enableBackgroundingOnSocket])
        {
            NSLog(@"Enabled backgrounding on socket");
        }
        else
        {
            NSLog(@"Enabling backgrounding failed!");
        }
    }];
    [self sendDataViaTCP];
}

- (void)socketDidSecure:(GCDAsyncSocket *)sock
{
}

- (void)socket:(GCDAsyncSocket *)sock didWriteDataWithTag:(long)tag
{
	NSLog(@"socket:%p didWriteDataWithTag:%ld", sock, tag);
}

- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
	NSLog(@"socket:%p didReadData:withTag:%ld", sock, tag);
	
	NSString *httpResponse = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
	
	NSLog(@"HTTP Response:\n%@", httpResponse);
	
}

- (void)socketDidDisconnect:(GCDAsyncSocket *)sock withError:(NSError *)err
{
	NSLog(@"socketDidDisconnect:%p withError: %@", sock, err);
}

@end
