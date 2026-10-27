@echo off

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Copyright (c) 2010, 2026 Contributors to the Eclipse Foundation
::
:: See the NOTICE file(s) distributed with this work for additional
:: information regarding copyright ownership.
::
:: This program and the accompanying materials are made available under the terms
:: of the MIT License which is available at https://opensource.org/licenses/MIT
::
:: SPDX-License-Identifier: MIT
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

"%~dp0/../eclipse-escetc.exe" -vm "%~dp0\..\java21\bin\javaw.exe" --launcher.noRestart --launcher.suppressErrors -application org.eclipse.escet.common.app.framework.application -nosplash org.eclipse.escet.cif.plcgen org.eclipse.escet.cif.plcgen.CifPlcGenApp %*
