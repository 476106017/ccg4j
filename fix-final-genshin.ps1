$ErrorActionPreference = "Continue"

# 需要修复的文件和类名映射
$fixes = @{
    "src\main\java\org\example\morecard\genshin\amulet\FakeSky.java" = @("FakeSky", "Hei")
    "src\main\java\org\example\morecard\genshin\follow\Diluc.java" = @("SearingOnslaught", "Dawn")
    "src\main\java\org\example\morecard\genshin\follow\Keaya.java" = @("Frostgnaw", "GlacialWaltz")
    "src\main\java\org\example\morecard\genshin\follow\Sucrose.java" = @("AnemoHypostasisCreation6308", "ForbiddenCreationIsomer75", "LargeWindSpirit")
    "src\main\java\org\example\morecard\genshin\LittlePrincess.java" = @("NormalAttack", "SwapCharacter")
    "src\main\java\org\example\morecard\genshin\spell\DawnOfWinery.java" = @("DawnOfWineryAmulet")
    "src\main\java\org\example\morecard\genshin\spell\ForgeSummon.java" = @("ForgeSpirit")
}

foreach ($file in $fixes.Keys) {
    Write-Host "`nProcessing: $(Split-Path $file -Leaf)"
    
    $content = Get-Content $file -Raw -Encoding UTF8
    
    foreach ($className in $fixes[$file]) {
        # 匹配类定义并添加rarity
        $pattern = "((?:public|private|protected)\s+(?:static\s+)?class\s+$className\s+[^{]*\{\s*(?:\r?\n)?)(\s*)([^}])"
        
        if ($content -match $pattern -and $content -notmatch "class\s+$className[^}]*?private CardRarity rarity") {
            # 在类定义后的第一行添加rarity
            $content = $content -replace $pattern, "`$1`$2private CardRarity rarity = CardRarity.BRONZE;`r`n`$2`$3"
            Write-Host "  Added rarity to: $className"
        }
    }
    
    [System.IO.File]::WriteAllText($file, $content, (New-Object System.Text.UTF8Encoding $false))
}

Write-Host "`nDone! Recompiling..."
& .\gradlew.bat compileJava 2>&1 | Select-String "BUILD" | Select-Object -Last 2
