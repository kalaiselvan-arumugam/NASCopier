# Running a JAR File Using a Batch Script and Task Scheduler

## Step 1: Create a Batch File to Run the JAR

Create a batch file (`NASCopier.bat`) with the following content:

``` bash
@echo off
java -Xmx512m -Dfile.encoding=UTF-8 -jar C:\path\to\your\NASCopier.jar
```
Save this file in a convenient location.

---
## Step 2: Configure Task Scheduler

1. Open **Task Scheduler** (`taskschd.msc`).
2. Click on **Create Basic Task**.
3. Set a **Name** and **Description** for the task.
4. Click **Next** and set the **Trigger**:
   - Choose your preferred schedule (e.g., **Daily**, **Hourly**).
5. Click **Next** and set the **Action**:
   - Select **Start a Program**.
   - Browse and select the `NASCopier.bat` file.
6. Click **Next**, review the settings, and click **Finish**.

Your JAR file will now execute automatically at the specified interval.
